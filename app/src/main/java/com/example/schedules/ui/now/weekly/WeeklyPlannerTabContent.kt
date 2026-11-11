package com.example.schedules.ui.now.weekly

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmRoast
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Obsolete WeeklyPlannerTabContent composable retained for backward compatibility.
 * Replaced by the unified NowTabScreen.
 */
@Deprecated("Use NowTabScreen directly for the unified interface")
@Composable
fun WeeklyPlannerTabContent(
    weekSchedule: WeekSchedule,
    selectedDate: LocalDate,
    weekStart: LocalDate,
    selectedDay: String,
    currentTime: LocalTime,
    notesMap: Map<Long, String>,
    notesDates: Set<LocalDate>,
    onDateSelected: (LocalDate, String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onEditClass: (CourseClass, String) -> Unit,
    onDeleteClass: (CourseClass) -> Unit,
    onSaveNote: (Long, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    NowTabScreen(
        status = ScheduleStatus(
            courseClass = null,
            dayName = selectedDay,
            status = ClassStatus.NONE,
            remainingFormatted = "00:00:00",
            progressPercent = 0f,
            hasClassesToday = false
        ),
        weekSchedule = weekSchedule,
        selectedDate = selectedDate,
        weekStart = weekStart,
        currentTime = currentTime,
        notesMap = notesMap,
        notesDates = notesDates,
        onDateSelected = onDateSelected,
        onTodayClick = onTodayClick,
        onCalendarClick = onCalendarClick,
        onEditClass = onEditClass,
        onDeleteClass = onDeleteClass,
        onSaveNote = onSaveNote,
        onDeleteNote = onDeleteNote
    )
}

@Composable
fun CoffeeCalendarDialog(
    initialDate: LocalDate,
    notesDates: Set<LocalDate> = emptySet(),
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedCalendarDate by remember { mutableStateOf(initialDate) }
    var displayedMonth by remember { mutableStateOf(initialDate.withDayOfMonth(1)) }

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = SteamedMilk,
            border = BorderStroke(1.25.dp, WarmRoast.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = DeepEspresso
                        )
                    }

                    Box(
                        modifier = Modifier.width(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = displayedMonth,
                            transitionSpec = {
                                if (targetState.isAfter(initialState)) {
                                    (slideInVertically(animationSpec = tween(200)) { height -> height / 2 } + fadeIn(tween(180)))
                                        .togetherWith(
                                            slideOutVertically(animationSpec = tween(200)) { height -> -height / 2 } + fadeOut(tween(180))
                                        )
                                } else {
                                    (slideInVertically(animationSpec = tween(200)) { height -> -height / 2 } + fadeIn(tween(180)))
                                        .togetherWith(
                                            slideOutVertically(animationSpec = tween(200)) { height -> height / 2 } + fadeOut(tween(180))
                                        )
                                }
                            },
                            label = "DialogMonthTransition"
                        ) { targetMonth ->
                            Text(
                                text = targetMonth.format(monthFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = DeepEspresso
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkMocha.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                val daysInMonth = remember(displayedMonth) {
                    val yearMonth = java.time.YearMonth.from(displayedMonth)
                    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
                    val paddingDays = firstDayOfWeek - 1
                    val lengthOfMonth = yearMonth.lengthOfMonth()

                    val list = mutableListOf<LocalDate?>()
                    repeat(paddingDays) { list.add(null) }
                    for (day in 1..lengthOfMonth) {
                        list.add(displayedMonth.withDayOfMonth(day))
                    }
                    list
                }

                val weeks = daysInMonth.chunked(7)

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (i in 0 until 7) {
                                val date = week.getOrNull(i)
                                if (date != null) {
                                    val isSelected = date == selectedCalendarDate
                                    val isToday = date == LocalDate.now()
                                    val hasNote = notesDates.contains(date)

                                    val cellBg = if (isSelected) DeepEspresso else Color.Transparent
                                    val textColor = if (isSelected) SteamedMilk else DarkMocha

                                    Surface(
                                        onClick = { selectedCalendarDate = date },
                                        shape = CircleShape,
                                        color = cellBg,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = textColor,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (hasNote) WarmRoast else Color.Transparent)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkMocha)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onDateSelected(selectedCalendarDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepEspresso)
                    ) {
                        Text("Select", color = SteamedMilk, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
