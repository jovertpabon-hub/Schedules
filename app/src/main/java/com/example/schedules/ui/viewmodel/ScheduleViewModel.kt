package com.example.schedules.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.ClassList
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.data.models.toEntity
import com.example.schedules.data.parser.IScheduleParser
import com.example.schedules.data.parser.PatternBasedScheduleParser
import com.example.schedules.data.pdf.AndroidPdfExtractor
import com.example.schedules.data.pdf.IPdfExtractor
import com.example.schedules.data.repository.IScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * Events that happen once, like showing a message or vibrating the phone.
 * We use a "SharedFlow" for these so they don't get repeated if the screen rotates.
 */
sealed class UiEvent {
    data class ShowMessage(val text: String, val actionText: String? = null, val onActionClick: (() -> Unit)? = null) : UiEvent()
    object VibrateError : UiEvent()
    object CloseDialog : UiEvent()
}

/**
 * The ScheduleViewModel is the "Manager" for the Schedule Screen.
 * It holds the data (State) and handles user actions (Logic).
 */
class ScheduleViewModel(
    private val repository: IScheduleRepository,
    context: Context
) : ViewModel() {

    private val appContext = context.applicationContext

    // Component instantiation using application Context
    private val pdfExtractor: IPdfExtractor = AndroidPdfExtractor(appContext)
    private val scheduleParser: IScheduleParser = PatternBasedScheduleParser()

    // --- UI States (Things the user sees) ---

    // The full list of classes for the whole week
    private val _weekSchedule = MutableStateFlow(WeekSchedule(emptyList()))
    val weekSchedule: StateFlow<WeekSchedule> = _weekSchedule.asStateFlow()

    // Loading state for initial startup skeleton presentation
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Information about the class happening RIGHT NOW (or the next one)
    private val _liveStatus = MutableStateFlow(createEmptyStatus())
    val liveStatus: StateFlow<ScheduleStatus> = _liveStatus.asStateFlow()

    // The list of classes left for the current day
    private val _remainingClassesToday = MutableStateFlow(ClassList(emptyList()))

    // Error messages shown when adding/editing
    private val _inputErrorMessage = MutableStateFlow<String?>(null)
    val inputErrorMessage: StateFlow<String?> = _inputErrorMessage.asStateFlow()


    // Stream of one-time events (like Snackbars)
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    // Temporary storage to allow "Undo" after deleting a class
    private var recentlyDeletedClass: ScheduleEntity? = null

    init {
        // Start these as soon as the ViewModel is created
        monitorDatabaseChanges()
        startLiveUpdatesTimer()
    }

    /**
     * Step 1: Data Monitoring
     * This function listens to the Repository. Whenever the database changes,
     * it automatically updates our _weekSchedule state.
     */
    private fun monitorDatabaseChanges() {
        viewModelScope.launch {
            repository.getAllSchedules().collect { allEntities ->
                // Group the raw database rows by lowercase day name for O(1) map lookups
                val groupedByDay = allEntities.groupBy { it.dayName.lowercase(Locale.ROOT) }
                
                // Ensure every day of the week exists in the list, preserving standard DayOfWeek order
                val finalWeekData = ScheduleConstants.DaysOfWeek.map { dayName ->
                    val dayEntities = groupedByDay[dayName.lowercase(Locale.ROOT)] ?: emptyList()
                    DayScheduleData(
                        dayName = dayName,
                        classes = dayEntities.map { it.toUiModel() }
                    )
                }
                
                _weekSchedule.value = WeekSchedule(finalWeekData)
                _isLoading.value = false
            }
        }
    }

    /**
     * Step 2: Live Updates
     * A timer that updates "Time Remaining" for the current class.
     * Only emits state updates when values actually change to prevent unnecessary recompositions.
     */
    private fun startLiveUpdatesTimer() {
        viewModelScope.launch {
            weekSchedule.collectLatest { schedule ->
                if (schedule.days.isEmpty()) {
                    _liveStatus.value = createEmptyStatus()
                    _remainingClassesToday.value = ClassList(emptyList())
                    return@collectLatest
                }
                
                while (true) {
                    val (newStatus, remainingList) = calculateCurrentStatus(schedule.days)
                    
                    if (_liveStatus.value != newStatus) {
                        _liveStatus.value = newStatus
                    }
                    if (_remainingClassesToday.value.items != remainingList) {
                        _remainingClassesToday.value = ClassList(remainingList)
                    }

                    delay(1.seconds)
                }
            }
        }
    }

    /**
     * Helper logic to figure out which class is Ongoing or Upcoming.
     */
    private fun calculateCurrentStatus(allDays: List<DayScheduleData>): Pair<ScheduleStatus, List<CourseClass>> {
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()
        val todayName = currentDate.dayOfWeek.name

        val todaySchedule = allDays.firstOrNull { it.dayName.equals(todayName, ignoreCase = true) }
        val todayClasses = todaySchedule?.classes ?: emptyList()

        val status = ScheduleStatusCalculator.determineScheduleStatus(todayClasses, currentDate, currentTime)
        val remainingClasses = todayClasses
            .filter { currentTime.isBefore(it.endTime) }
            .sortedBy { it.startTime }

        return status to remainingClasses
    }

    // --- User Actions (CRUD) ---

    fun importPdf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawText = pdfExtractor.extractText(uri)
                val extractedSchedules = scheduleParser.parse(rawText)

                if (extractedSchedules.isEmpty()) {
                    _uiEvents.emit(UiEvent.ShowMessage("No schedules found in PDF"))
                } else {
                    var successCount = 0
                    extractedSchedules.forEach { extracted ->
                        val entity = extracted.toEntity()
                        repository.validateAndSaveSchedule(entity)
                            .onSuccess { successCount++ }
                    }
                    _uiEvents.emit(UiEvent.ShowMessage("Imported $successCount classes from PDF"))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowMessage("Error importing PDF: ${e.message}"))
            }
        }
    }

    fun addNewOrEditSchedule(
        existingId: Long? = null,
        day: String,
        code: String,
        name: String,
        start: LocalTime,
        end: LocalTime,
        room: String
    ) {
        viewModelScope.launch {
            _inputErrorMessage.value = null // Reset errors
            
            // Basic check: Are all boxes filled?
            if (day.isBlank() || code.isBlank() || name.isBlank() || room.isBlank()) {
                _inputErrorMessage.value = "Please fill in all details"
                _uiEvents.emit(UiEvent.VibrateError)
                return@launch
            }

            val scheduleToSave = ScheduleEntity(
                id = existingId ?: ScheduleConstants.DEFAULT_ID,
                dayName = day,
                courseCode = code,
                courseName = name,
                startTime = start,
                endTime = end,
                room = room
            )

            // Tell the repository to validate and save
            repository.validateAndSaveSchedule(scheduleToSave)
                .onSuccess {
                    _uiEvents.emit(UiEvent.ShowMessage(if (existingId == null) "Class added!" else "Class updated!"))
                    _uiEvents.emit(UiEvent.CloseDialog)
                }
                .onFailure { error ->
                    _inputErrorMessage.value = error.message
                    _uiEvents.emit(UiEvent.VibrateError)
                }
        }
    }

    fun deleteClass(classToDelete: ScheduleEntity) {
        viewModelScope.launch {
            recentlyDeletedClass = classToDelete
            repository.deleteSchedule(classToDelete)
            
            _uiEvents.emit(UiEvent.ShowMessage(
                text = "Class removed",
                actionText = "Undo",
                onActionClick = { restoreDeletedClass() }
            ))
        }
    }

    fun clearAllSchedules() {
        viewModelScope.launch {
            repository.clearAllSchedules()
            _uiEvents.emit(UiEvent.ShowMessage("All schedules cleared"))
        }
    }

    private fun restoreDeletedClass() {
        viewModelScope.launch {
            recentlyDeletedClass?.let {
                repository.saveScheduleDirectly(it)
                recentlyDeletedClass = null
            }
        }
    }

    // --- Helper Functions ---

    private fun createEmptyStatus(hasClassesToday: Boolean = false) = ScheduleStatus(
        courseClass = null,
        dayName = LocalDate.now().dayOfWeek.name,
        status = ClassStatus.NONE,
        remainingFormatted = "00:00:00",
        hasClassesToday = hasClassesToday
    )

    // Extension function to convert Database Entity -> UI Model
    private fun ScheduleEntity.toUiModel() = CourseClass(
        id = this.id,
        courseCode = this.courseCode,
        courseName = this.courseName,
        startTime = this.startTime,
        endTime = this.endTime,
        room = this.room
    )

    /**
     * Factory class tells Android how to create our ViewModel with a Repository.
     */
    class Factory(
        private val repository: IScheduleRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ScheduleViewModel(repository, context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

object ScheduleStatusCalculator {
    fun determineScheduleStatus(
        classes: List<CourseClass>,
        currentDate: LocalDate,
        currentTime: LocalTime
    ): ScheduleStatus {
        val dayName = currentDate.dayOfWeek.name

        if (classes.isEmpty()) {
            return ScheduleStatus(
                courseClass = null,
                dayName = dayName,
                status = ClassStatus.NONE,
                remainingFormatted = "00:00:00",
                progressPercent = 0f,
                hasClassesToday = false
            )
        }

        val sortedClasses = classes.sortedBy { it.startTime }

        for (course in sortedClasses) {
            // Boundary condition: class is relevant if current time is strictly before end time
            if (currentTime.isBefore(course.endTime)) {
                if (!currentTime.isBefore(course.startTime)) {
                    // ONGOING: currentTime >= course.startTime && currentTime < course.endTime
                    val timeLeft = maxOf(0L, Duration.between(currentTime, course.endTime).seconds)
                    val totalDuration = Duration.between(course.startTime, course.endTime).seconds
                    val progress = if (totalDuration > 0) (totalDuration - timeLeft).toFloat() / totalDuration.toFloat() else 0f

                    return ScheduleStatus(
                        courseClass = course,
                        dayName = dayName,
                        status = ClassStatus.ONGOING,
                        remainingFormatted = formatTimeRemaining(timeLeft),
                        progressPercent = progress.coerceIn(0f, 1f),
                        hasClassesToday = true
                    )
                } else {
                    // UPCOMING: currentTime < course.startTime
                    val timeUntilStart = maxOf(0L, Duration.between(currentTime, course.startTime).seconds)
                    return ScheduleStatus(
                        courseClass = course,
                        dayName = dayName,
                        status = ClassStatus.UPCOMING,
                        remainingFormatted = formatTimeRemaining(timeUntilStart),
                        progressPercent = 0f,
                        hasClassesToday = true
                    )
                }
            }
        }

        // All classes have ended (currentTime >= all course.endTime)
        return ScheduleStatus(
            courseClass = null,
            dayName = dayName,
            status = ClassStatus.NONE,
            remainingFormatted = "00:00:00",
            progressPercent = 0f,
            hasClassesToday = true
        )
    }

    fun formatTimeRemaining(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }
}
