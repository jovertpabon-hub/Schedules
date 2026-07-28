package com.example.schedules.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedules.data.models.ClassList
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.data.repository.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class ScheduleViewModel : ViewModel() {

    private val _weekData = MutableStateFlow(WeekSchedule(ScheduleRepository.getWeekData()))
    val weekData: StateFlow<WeekSchedule> = _weekData.asStateFlow()

    private val _currentStatus = MutableStateFlow(calculateInitialStatus())
    val currentStatus: StateFlow<ScheduleStatus> = _currentStatus.asStateFlow()

    private val _todayRemainingClasses = MutableStateFlow(ClassList(emptyList()))
    val todayRemainingClasses: StateFlow<ClassList> = _todayRemainingClasses.asStateFlow()

    init {
        startTimer()
    }

    private fun calculateInitialStatus(): ScheduleStatus {
        return ScheduleStatus(
            courseClass = null,
            dayName = LocalDate.now().dayOfWeek.name,
            status = ClassStatus.NONE,
            remainingFormatted = "Rest Up"
        )
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val data = _weekData.value
                val (status, remaining) = updateScheduleState(data.days)
                
                _currentStatus.value = status
                // Optimization: Only update flow if the list content changed
                val newRemaining = ClassList(remaining)
                if (_todayRemainingClasses.value != newRemaining) {
                    _todayRemainingClasses.value = newRemaining
                }

                val nextDelay = if (status.status == ClassStatus.NONE) 60.seconds else 1.seconds
                delay(nextDelay)
            }
        }
    }

    private fun updateScheduleState(weekData: List<DayScheduleData>): Pair<ScheduleStatus, List<CourseClass>> {
        val today = LocalDate.now().dayOfWeek.name
        val now = LocalTime.now()

        val todaySchedule = weekData.firstOrNull { it.dayName.equals(today, ignoreCase = true) }
        
        if (todaySchedule == null || todaySchedule.classes.isEmpty()) {
            return calculateInitialStatus() to emptyList()
        }

        var currentStatusResult: ScheduleStatus? = null
        val remainingClassesResult = mutableListOf<CourseClass>()

        for (course in todaySchedule.classes) {
            if (now.isBefore(course.endTime)) {
                remainingClassesResult.add(course)

                if (currentStatusResult == null) {
                    if (now.isAfter(course.startTime) || now == course.startTime) {
                        val remainingSeconds = Duration.between(now, course.endTime).seconds
                        val totalSeconds = Duration.between(course.startTime, course.endTime).seconds
                        val progress = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat() else 0f
                        
                        currentStatusResult = ScheduleStatus(
                            courseClass = course,
                            dayName = todaySchedule.dayName,
                            status = ClassStatus.ONGOING,
                            remainingFormatted = formatSeconds(remainingSeconds),
                            progressPercent = progress
                        )
                    } else if (now.isBefore(course.startTime)) {
                        val remainingSeconds = Duration.between(now, course.startTime).seconds
                        currentStatusResult = ScheduleStatus(
                            courseClass = course,
                            dayName = todaySchedule.dayName,
                            status = ClassStatus.UPCOMING,
                            remainingFormatted = formatSeconds(remainingSeconds)
                        )
                    }
                }
            }
        }

        val finalStatus = currentStatusResult ?: calculateInitialStatus()
        return finalStatus to remainingClassesResult
    }

    private fun formatSeconds(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }
}
