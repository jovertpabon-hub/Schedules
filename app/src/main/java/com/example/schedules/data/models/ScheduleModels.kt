package com.example.schedules.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScheduleTab(val title: String, val icon: ImageVector) {
    NOW("Now", Icons.Default.Schedule),
    GRID("Cards View", Icons.Default.GridView),
    TIMETABLE("Matrix View", Icons.Default.TableChart),
    TEMPLATE("Template", Icons.Default.DateRange)
}

data class CourseClass(
    val courseCode: String,
    val courseName: String,
    val startTime: String,
    val endTime: String,
    val room: String = "TBA",
    val instructor: String
)

data class DayScheduleData(
    val dayName: String,
    val classes: List<CourseClass>
)

enum class ClassStatus {
    ONGOING,
    UPCOMING,
    NONE
}

data class ScheduleStatus(
    val courseClass: CourseClass?,
    val dayName: String,
    val status: ClassStatus,
    val remainingFormatted: String,
    val progressPercent: Float = 0f
)
