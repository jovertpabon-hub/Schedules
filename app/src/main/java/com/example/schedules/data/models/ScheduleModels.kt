package com.example.schedules.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ScheduleConstants {
    val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
}

enum class ScheduleTab(val title: String, val icon: ImageVector) {
    NOW("Now", Icons.Default.Schedule),
    GRID("Cards View", Icons.Default.GridView)
}

@Immutable
data class CourseClass(
    val courseCode: String,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String = "TBA",
    val instructor: String
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
