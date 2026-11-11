package com.example.schedules.ui.now.today

import androidx.compose.runtime.Composable
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.components.NowTabScreen
import java.time.LocalDate
import java.time.LocalTime

/**
 * Obsolete TodayTabContent composable retained for backward compatibility.
 * Replaced by the unified NowTabScreen.
 */
@Deprecated("Use NowTabScreen directly for the unified interface")
@Composable
fun TodayTabContent(
    status: ScheduleStatus,
    weekSchedule: WeekSchedule,
    currentTime: LocalTime,
    notesMap: Map<Long, String>,
    onEditClass: (CourseClass, String) -> Unit,
    onDeleteClass: (CourseClass) -> Unit,
    onSaveNote: (Long, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    NowTabScreen(
        status = status,
        weekSchedule = weekSchedule,
        selectedDate = LocalDate.now(),
        currentTime = currentTime,
        notesMap = notesMap,
        onEditClass = onEditClass,
        onDeleteClass = onDeleteClass,
        onSaveNote = onSaveNote,
        onDeleteNote = onDeleteNote
    )
}
