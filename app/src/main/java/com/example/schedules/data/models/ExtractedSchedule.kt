package com.example.schedules.data.models

import java.time.DayOfWeek
import java.time.LocalTime

data class ExtractedSchedule(
    val courseCode: String,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String = "TBA",
    val dayOfWeek: DayOfWeek,
    val instructor: String = "Unknown"
)
