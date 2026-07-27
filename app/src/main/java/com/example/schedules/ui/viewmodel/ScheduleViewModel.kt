package com.example.schedules.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.repository.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@RequiresApi(Build.VERSION_CODES.O)
class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _weekData = MutableStateFlow(repository.getWeekData())
    val weekData: StateFlow<List<DayScheduleData>> = _weekData.asStateFlow()

    private val _currentStatus = MutableStateFlow(calculateCurrentStatus(_weekData.value))
    val currentStatus: StateFlow<ScheduleStatus> = _currentStatus.asStateFlow()

    private val _todayRemainingClasses = MutableStateFlow(calculateTodayRemainingClasses(_weekData.value))
    val todayRemainingClasses: StateFlow<List<CourseClass>> = _todayRemainingClasses.asStateFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val data = _weekData.value
                _currentStatus.value = calculateCurrentStatus(data)
                _todayRemainingClasses.value = calculateTodayRemainingClasses(data)
                delay(1000.milliseconds)
            }
        }
    }

    private fun calculateCurrentStatus(weekData: List<DayScheduleData>): ScheduleStatus {
        val today = LocalDate.now().dayOfWeek.name
        val now = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

        val todaySchedule = weekData.firstOrNull { it.dayName.equals(today, ignoreCase = true) }

        if (todaySchedule != null) {
            // 1. Check for ONGOING class today
            for (course in todaySchedule.classes) {
                val startTime = runCatching { LocalTime.parse(course.startTime, timeFormatter) }.getOrNull()
                val endTime = runCatching { LocalTime.parse(course.endTime, timeFormatter) }.getOrNull()

                if (startTime != null && endTime != null && (now.isAfter(startTime) || now == startTime) && now.isBefore(endTime)) {
                    val remainingSeconds = Duration.between(now, endTime).seconds
                    val totalSeconds = Duration.between(startTime, endTime).seconds
                    val elapsed = totalSeconds - remainingSeconds
                    val progress = if (totalSeconds > 0) elapsed.toFloat() / totalSeconds.toFloat() else 0f

                    val hours = remainingSeconds / 3600
                    val minutes = (remainingSeconds % 3600) / 60
                    val seconds = remainingSeconds % 60

                    return ScheduleStatus(
                        courseClass = course,
                        dayName = todaySchedule.dayName,
                        status = ClassStatus.ONGOING,
                        remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                        progressPercent = progress
                    )
                }
            }

            // 2. Check for UPCOMING class TODAY
            val upcomingToday = todaySchedule.classes.firstOrNull { course ->
                val startTime = runCatching { LocalTime.parse(course.startTime, timeFormatter) }.getOrNull()
                startTime != null && now.isBefore(startTime)
            }

            if (upcomingToday != null) {
                val startTime = LocalTime.parse(upcomingToday.startTime, timeFormatter)
                val remainingSeconds = Duration.between(now, startTime).seconds

                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val seconds = remainingSeconds % 60

                return ScheduleStatus(
                    courseClass = upcomingToday,
                    dayName = todaySchedule.dayName,
                    status = ClassStatus.UPCOMING,
                    remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                )
            }
        }

        return ScheduleStatus(
            courseClass = null,
            dayName = today,
            status = ClassStatus.NONE,
            remainingFormatted = "End of the day"
        )
    }

    private fun calculateTodayRemainingClasses(weekData: List<DayScheduleData>): List<CourseClass> {
        val today = LocalDate.now().dayOfWeek.name
        val now = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

        val todaySchedule = weekData.firstOrNull { it.dayName.equals(today, ignoreCase = true) }

        return todaySchedule?.classes?.filter { course ->
            val endTime = runCatching { LocalTime.parse(course.endTime, timeFormatter) }.getOrNull()
            endTime != null && now.isBefore(endTime)
        } ?: emptyList()
    }
}
