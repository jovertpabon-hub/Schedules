package com.example.schedules.data.models

import androidx.compose.runtime.Immutable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ScheduleConstants {
    val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    
    val DaysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    const val DEFAULT_ID = 0L
}


@Immutable
data class CourseClass(
    val id: Long? = null,
    val courseCode: String,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String = "TBA"
)

@Immutable
data class DayScheduleData(
    val dayName: String,
    val classes: List<CourseClass>
)

// Wrapper for List stability in Compose
@Immutable
data class WeekSchedule(
    val days: List<DayScheduleData>
)

@Immutable
data class ClassList(
    val items: List<CourseClass>
)

enum class ClassStatus {
    ONGOING,
    UPCOMING,
    NONE
}

@Immutable
data class ScheduleStatus(
    val courseClass: CourseClass?,
    val dayName: String,
    val status: ClassStatus,
    val remainingFormatted: String,
    val progressPercent: Float = 0f
)
