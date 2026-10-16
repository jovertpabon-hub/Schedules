package com.example.schedules.data.models

import com.example.schedules.data.local.ScheduleEntity
import java.time.format.TextStyle
import java.util.Locale

/**
 * Utility functions to map between different schedule models.
 */

fun ExtractedSchedule.toEntity(): ScheduleEntity {
    // Convert DayOfWeek Enum to String (e.g. "Monday")
    val dayName = this.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
    
    return ScheduleEntity(
        dayName = dayName,
        courseCode = this.courseCode,
        courseName = this.courseName,
        startTime = this.startTime,
        endTime = this.endTime,
        room = this.room.ifEmpty { "TBA" }
    )
}
