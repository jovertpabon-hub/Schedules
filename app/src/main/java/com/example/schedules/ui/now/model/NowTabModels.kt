package com.example.schedules.ui.now.model

import java.time.LocalTime

data class VacantTimeBlock(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long
)

enum class NowSubTab(val title: String) {
    TODAY("Today"),
    WEEKLY_PLANNER("Weekly Planner")
}
