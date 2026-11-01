package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.theme.AppBackground
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmCoffeeCream
import com.example.schedules.ui.theme.WarmRoast
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

data class VacantTimeBlock(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long
)

/**
 * Merges overlapping or contiguous class time intervals into a list of non-overlapping time spans.
 */
fun mergedClassIntervals(classes: List<CourseClass>): List<Pair<LocalTime, LocalTime>> {
    if (classes.isEmpty()) return emptyList()

    val sorted = classes.sortedWith(compareBy({ it.startTime }, { it.endTime }))
    val merged = mutableListOf<Pair<LocalTime, LocalTime>>()

    var currentStart = sorted[0].startTime
    var currentEnd = sorted[0].endTime

    for (i in 1 until sorted.size) {
        val course = sorted[i]
        if (!course.startTime.isAfter(currentEnd)) {
            if (course.endTime.isAfter(currentEnd)) {
                currentEnd = course.endTime
            }
        } else {
            merged.add(currentStart to currentEnd)
            currentStart = course.startTime
            currentEnd = course.endTime
        }
    }
    merged.add(currentStart to currentEnd)
    return merged
}

fun calculateTotalClassHours(classes: List<CourseClass>): String {
    val merged = mergedClassIntervals(classes)
    val totalMinutes = merged.sumOf { (start, end) -> Duration.between(start, end).toMinutes() }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "$hours hr $mins m"
        hours > 0 -> "$hours hrs"
        else -> "$mins mins"
    }
}

fun calculateVacantTimeBlocks(classes: List<CourseClass>): List<VacantTimeBlock> {
    val defaultStart = LocalTime.of(8, 0)
    val defaultEnd = LocalTime.of(17, 0)

    if (classes.isEmpty()) {
        val duration = Duration.between(defaultStart, defaultEnd).toMinutes()
        return listOf(VacantTimeBlock(defaultStart, defaultEnd, duration))
    }

    val merged = mergedClassIntervals(classes)
    val earliestStart = merged.first().first
    val latestEnd = merged.maxOf { it.second }

    val windowStart = if (earliestStart.isBefore(defaultStart)) earliestStart else defaultStart
    val windowEnd = if (latestEnd.isAfter(defaultEnd)) latestEnd else defaultEnd

    val vacantBlocks = mutableListOf<VacantTimeBlock>()
    var currentPointer = windowStart

    for ((cStart, cEnd) in merged) {
        if (cStart.isAfter(windowEnd)) break

        if (cStart.isAfter(currentPointer)) {
            val gapMinutes = Duration.between(currentPointer, cStart).toMinutes()
            if (gapMinutes > 0) {
                vacantBlocks.add(VacantTimeBlock(currentPointer, cStart, gapMinutes))
            }
        }

        if (cEnd.isAfter(currentPointer)) {
            currentPointer = cEnd
        }
    }

    if (currentPointer.isBefore(windowEnd)) {
        val gapMinutes = Duration.between(currentPointer, windowEnd).toMinutes()
        if (gapMinutes > 0) {
            vacantBlocks.add(VacantTimeBlock(currentPointer, windowEnd, gapMinutes))
        }
    }

    return vacantBlocks
}

fun calculateTotalFreeTime(vacantBlocks: List<VacantTimeBlock>): String {
    val totalMinutes = vacantBlocks.sumOf { it.durationMinutes }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "$hours hr $mins m"
        hours > 0 -> "$hours hrs"
        else -> "$mins mins"
    }
}

@Composable
fun NowTabScreen(
    status: ScheduleStatus,
    weekSchedule: WeekSchedule,
    selectedDate: LocalDate,
    weekStart: LocalDate,
    selectedDay: String,
    onDateSelected: (LocalDate, String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit,
    onEditClass: (CourseClass, String) -> Unit = { _, _ -> },
    onDeleteClass: (CourseClass) -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = LocalTime.now()
            delay(1.seconds)
        }
    }

    var showVacantTimes by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    if (showCalendarDialog) {
        CoffeeCalendarDialog(
            initialDate = selectedDate,
            onDismiss = { showCalendarDialog = false },
            onDateSelected = { date ->
                val dayName = ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(date.dayOfWeek.name, ignoreCase = true) } ?: "Monday"
                onDateSelected(date, dayName)
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header sitting directly on AppBackground
            NowTabHeader(
                currentDate = selectedDate,
                onAboutClick = onAboutClick
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(1.5.dp)
                    .fillMaxWidth(),
                color = DarkMocha.copy(alpha= 0.25f)
            )

            // Redesigned Weekly Calendar Strip inside a WarmCoffeeCream surface
            WeeklyCalendarStrip(
                weekStart = weekStart,
                selectedDate = selectedDate,
                weekSchedule = weekSchedule,
                onDateSelected = { date, dayName ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDateSelected(date, dayName)
                },
                onPreviousWeek = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPreviousWeek()
                },
                onNextWeek = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNextWeek()
                },
                onTodayClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTodayClick()
                },
                onCalendarClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCalendarDialog = true
                }
            )

            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing))
                        .togetherWith(fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing)))
                },
                label = "DayContentTransition",
                modifier = Modifier.fillMaxWidth()
            ) { currentDay ->
                val currentDayData = remember(weekSchedule, currentDay) {
                    val daySchedule = weekSchedule.days.firstOrNull { it.dayName.equals(currentDay, ignoreCase = true) }
                    DayScheduleData(
                        dayName = currentDay,
                        classes = daySchedule?.classes?.sortedBy { it.startTime } ?: emptyList()
                    )
                }
                val isTodayCurrent = selectedDate == LocalDate.now()
                val currentVacantBlocks = remember(currentDayData.classes) {
                    calculateVacantTimeBlocks(currentDayData.classes)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTodayCurrent) {
                        NowTabCountdownCard(
                            status = status
                        )
                    } else {
                        DaySummaryHeroCard(
                            dayData = currentDayData
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier
                                .height(1.5.dp)
                                .fillMaxWidth(),
                            color = DarkMocha.copy(alpha= 0.25f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = if (showVacantTimes) "Free time" else "$currentDay classes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepEspresso
                                )
                                Text(
                                    text = if (showVacantTimes) "${currentVacantBlocks.size} available gaps" else "${currentDayData.classes.size} scheduled",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DarkMocha.copy(alpha = 0.72f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = WarmCoffeeCream.copy(alpha = 0.72f)
                            ) {
                                Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    listOf(false to "Classes", true to "Free time").forEach { (freeTime, label) ->
                                        val selected = showVacantTimes == freeTime
                                        Surface(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                showVacantTimes = freeTime
                                            },
                                            shape = RoundedCornerShape(11.dp),
                                            color = if (selected) SteamedMilk else Color.Transparent
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) DeepEspresso else DarkMocha.copy(alpha = 0.78f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showVacantTimes) {
                            if (currentVacantBlocks.isEmpty()) {
                                EmptyDayView(
                                    modifier = Modifier.fillMaxWidth(),
                                    mainMessage = "No Free Time Gaps!",
                                    secondaryMessage = "Classes run back-to-back throughout the day."
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    currentVacantBlocks.forEach { block ->
                                        VacantTimeBlockCard(block = block)
                                    }
                                }
                            }
                        } else {
                            if (currentDayData.classes.isEmpty()) {
                                EmptyDayView(
                                    modifier = Modifier.fillMaxWidth(),
                                    mainMessage = "No classes for $currentDay!",
                                    secondaryMessage = "Enjoy a free day or catch up on studies ☕"
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    currentDayData.classes.forEach { course ->
                                        NowTabClassDetails(
                                            course = course,
                                            isToday = isTodayCurrent,
                                            currentTime = currentTime,
                                            onEdit = { onEditClass(course, currentDayData.dayName) },
                                            onDelete = { onDeleteClass(course) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NowTabHeader(
    currentDate: LocalDate,
    onAboutClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val formattedDate = remember(currentDate) {
        currentDate.format(
            DateTimeFormatter.ofPattern("EEEE • MMMM d", Locale.US)
        ).uppercase(Locale.US)
    }

    val currentHour = LocalTime.now().hour
    val greeting = when (currentHour) {
        in 5..11 -> "Good morning!"
        in 12..17 -> "Good afternoon!"
        else -> "Good evening!"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = DeepEspresso,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight
                        .Bold,
                    color = DarkMocha,
                    letterSpacing = 1.sp
                )
            }

            // Main greeting
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DarkMocha
            )
        }

        // Coffee icon
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAboutClick()
            },
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = SteamedMilk,
            shadowElevation = 1.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = "About",
                    tint = DeepEspresso,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun CoffeeCalendarDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate)
                    }
                    onDismiss()
                }
            ) {
                Text("Select", color = DeepEspresso, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkMocha)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = SteamedMilk
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = SteamedMilk,
                selectedDayContainerColor = DeepEspresso,
                selectedDayContentColor = SteamedMilk,
                todayContentColor = DeepEspresso,
                headlineContentColor = DeepEspresso,
                weekdayContentColor = DarkMocha,
                subheadContentColor = DarkMocha,
                yearContentColor = DeepEspresso
            )
        )
    }
}

@Composable
private fun WeeklyCalendarStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    weekSchedule: WeekSchedule,
    onDateSelected: (LocalDate, String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val weekEnd = weekStart.plusDays(6)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.US) }
    val yearFormatter = remember { DateTimeFormatter.ofPattern("yyyy", Locale.US) }
    
    val weekRangeText = remember(weekStart, weekEnd) {
        val startStr = weekStart.format(dateFormatter)
        val endStr = weekEnd.format(dateFormatter)
        val yearStr = weekStart.format(yearFormatter)
        "$startStr – $endStr, $yearStr"
    }

    val isCurrentWeek = remember(weekStart) {
        val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        weekStart == currentWeekStart
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = WarmCoffeeCream),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Left: Previous Week
                IconButton(
                    onClick = onPreviousWeek,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous week",
                        tint = DeepEspresso,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: Date Range & Retry Button
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onCalendarClick,
                        shape = RoundedCornerShape(8.dp),
                        color = SteamedMilk,
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Open Calendar",
                                tint = DeepEspresso,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = weekRangeText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        }
                    }

                    if (!isCurrentWeek) {
                        Surface(
                            onClick = onTodayClick,
                            shape = CircleShape,
                            color = DeepEspresso,
                            shadowElevation = 1.dp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset to Today",
                                    tint = SteamedMilk,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Right: Next Week
                IconButton(
                    onClick = onNextWeek,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next week",
                        tint = DeepEspresso,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 7 day items in one horizontal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScheduleConstants.DaysOfWeek.forEachIndexed { index, dayName ->
                    val dayDate = weekStart.plusDays(index.toLong())
                    val dateNumber = dayDate.dayOfMonth.toString()
                    val isSelectedDate = dayDate.equals(selectedDate)
                    val isToday = dayDate.equals(LocalDate.now())

                    val dayData = weekSchedule.days.firstOrNull { it.dayName.equals(dayName, ignoreCase = true) }
                    val classCount = dayData?.classes?.size ?: 0
                    val hasClasses = classCount > 0

                    val tileBg = if (isSelectedDate) DeepEspresso else SteamedMilk
                    val weekdayColor = if (isSelectedDate) SteamedMilk.copy(alpha = 0.9f) else DarkMocha.copy(alpha = 0.65f)
                    val dateColor = if (isSelectedDate) SteamedMilk else DarkMocha
                    val indicatorColor = if (isSelectedDate) GoldenCrema else WarmRoast

                    Surface(
                        onClick = { onDateSelected(dayDate, dayName) },
                        shape = RoundedCornerShape(14.dp),
                        color = tileBg,
                        border = if (!isSelectedDate && isToday) BorderStroke(1.dp, WarmRoast.copy(alpha = 0.6f)) else null,
                        shadowElevation = if (isSelectedDate) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = dayName.take(3).uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelectedDate) FontWeight.ExtraBold else FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline,
                                color = weekdayColor,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = dateNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = dateColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasClasses) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor)
                                    )
                                } else if (isToday && !isSelectedDate) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(DarkMocha.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySummaryHeroCard(
    dayData: DayScheduleData
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DeepEspresso
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = SteamedMilk,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "${dayData.dayName.uppercase(Locale.US)} OVERVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteamedMilk,
                    letterSpacing = 0.8.sp
                )
            }

            if (dayData.classes.isNotEmpty()) {
                val firstStart = dayData.classes.minOf { it.startTime }
                    .format(ScheduleConstants.TimeFormatter)

                val lastEnd = dayData.classes.maxOf { it.endTime }
                    .format(ScheduleConstants.TimeFormatter)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Class time",
                        tint = GoldenCrema,
                        modifier = Modifier.size(25.dp)
                    )

                    Text(
                        text = "$firstStart - $lastEnd",
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = GoldenCrema
                    )
                }
            } else {
                Text(
                    text = "No Classes Scheduled ☕",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldenCrema
                )
            }
        }
    }
}

@Composable
private fun NowTabCountdownCard(
    status: ScheduleStatus
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DeepEspresso
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                GoldenCrema.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        CountdownTimerView(status = status)
    }
}

@Composable
private fun CountdownTimerView(
    status: ScheduleStatus
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (status.status) {
                        ClassStatus.ONGOING -> Icons.Default.PlayCircleFilled
                        ClassStatus.UPCOMING -> Icons.Default.AccessTime
                        ClassStatus.NONE -> if (status.hasClassesToday) Icons.Default.CheckCircle else Icons.Default.CalendarMonth
                    },
                    contentDescription = null,
                    tint = GoldenCrema,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = when (status.status) {
                        ClassStatus.ONGOING -> "CLASS IN PROGRESS"
                        ClassStatus.UPCOMING -> "NEXT CLASS"
                        ClassStatus.NONE -> if (status.hasClassesToday) "SCHEDULE COMPLETED" else "NO CLASSES TODAY"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteamedMilk,
                    letterSpacing = 0.6.sp
                )
            }

            if (status.status != ClassStatus.NONE && status.courseClass != null) {
                Surface(
                    color = GoldenCrema,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status.courseClass.courseCode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepEspresso,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (status.status == ClassStatus.NONE || status.courseClass == null) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = GoldenCrema.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FreeBreakfast,
                            contentDescription = null,
                            tint = GoldenCrema,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (status.hasClassesToday)
                            "All Classes Finished"
                        else
                            "No Classes Today",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SteamedMilk
                    )

                    Text(
                        text = if (status.hasClassesToday)
                            "You're done for today. Enjoy your free time."
                        else
                            "Your schedule is clear today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteamedMilk.copy(alpha = 0.75f)
                    )
                }
            }
        } else {
            val course = status.courseClass

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (status.status == ClassStatus.ONGOING) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 6.dp.toPx()
                            drawArc(
                                color = Color(0xFF79552D).copy(alpha = 0.4f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = GoldenCrema,
                                startAngle = -90f,
                                sweepAngle = 360f * status.progressPercent.coerceIn(0f, 1f),
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 2.dp.toPx()
                            drawArc(
                                color = GoldenCrema.copy(alpha = 0.35f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = status.remainingFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = SteamedMilk,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (status.status == ClassStatus.ONGOING) "REMAINING" else "STARTS IN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SteamedMilk.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Class time",
                            tint = GoldenCrema,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${course.endTime.format(ScheduleConstants.TimeFormatter)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = SteamedMilk.copy(alpha = 0.85f)
                        )
                    }

                    if (course.room.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Room location",
                                tint = GoldenCrema,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = course.room,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SteamedMilk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VacantTimeBlockCard(
    block: VacantTimeBlock
) {
    val hours = block.durationMinutes / 60
    val mins = block.durationMinutes % 60
    val durationText = when {
        hours > 0 && mins > 0 -> "$hours hr $mins mins free"
        hours > 0 -> "$hours hr free"
        else -> "$mins mins free"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SteamedMilk.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = GoldenCrema.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FreeBreakfast,
                            contentDescription = "Free time",
                            tint = DeepEspresso,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Gap duration",
                        tint = DarkMocha.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${block.startTime.format(ScheduleConstants.TimeFormatter)} - ${block.endTime.format(ScheduleConstants.TimeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkMocha
                    )
                }
            }

            Surface(
                color = DeepEspresso.copy(alpha = 0.85f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SteamedMilk,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NowTabClassDetails(
    course: CourseClass,
    isToday: Boolean,
    currentTime: LocalTime,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isCompleted = isToday && !currentTime.isBefore(course.endTime)
    val isOngoing = isToday && !currentTime.isBefore(course.startTime) && currentTime.isBefore(course.endTime)

    val cardTextColor = when {
        isCompleted -> DarkMocha.copy(alpha = 0.55f)
        else -> DarkMocha
    }
    val iconTint = DarkMocha.copy(alpha = 0.65f)

    val cardBg = when {
        isCompleted -> SteamedMilk.copy(alpha = 0.7f)
        else -> SteamedMilk
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Class", fontWeight = FontWeight.Bold, color = DarkMocha) },
            text = { Text("Are you sure you want to delete ${course.courseName} (${course.courseCode})?", color = DarkMocha) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = DarkMocha)
                }
            },
            containerColor = SteamedMilk
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 1.dp else 0.dp),
        border = BorderStroke(
            width = 0.75.dp,
            color = when {
                isOngoing -> GoldenCrema.copy(alpha = 0.8f)
                isCompleted -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Course Code & Name (Primary & Secondary) + Menu options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = course.courseCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepEspresso
                        )

                        if (isToday && isOngoing) {
                            Surface(
                                color = DeepEspresso,
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "ONGOING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SteamedMilk,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isToday && isCompleted) {
                            Surface(
                                color = DarkMocha.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "DONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkMocha.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = cardTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SteamedMilk)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Class", fontWeight = FontWeight.Bold, color = DarkMocha) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = DarkMocha, modifier = Modifier.size(16.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Class", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = DarkMocha.copy(alpha = 0.12f)
            )

            // Row 2: Schedule & Room (Schedule Info & Additional Info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Class time",
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${course.endTime.format(ScheduleConstants.TimeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = cardTextColor
                    )
                }

                // Room Location
                if (course.room.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Room location",
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = course.room,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = cardTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NowTabThumbnailPreview() {
    val sampleClasses = listOf(
        CourseClass(id = 1, courseCode = "CS101", courseName = "Intro to Computer Science", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30), room = "Room 302"),
        CourseClass(id = 2, courseCode = "MATH202", courseName = "Calculus & Linear Algebra", startTime = LocalTime.of(11, 0), endTime = LocalTime.of(12, 30), room = "Lab 105"),
        CourseClass(id = 3, courseCode = "PHYS103", courseName = "General Physics Laboratory", startTime = LocalTime.of(14, 0), endTime = LocalTime.of(16, 0), room = "Physics Hall A")
    )

    val sampleDays = ScheduleConstants.DaysOfWeek.map { dayName ->
        DayScheduleData(dayName = dayName, classes = if (dayName == "Monday") sampleClasses else emptyList())
    }

    val sampleStatus = ScheduleStatus(
        courseClass = sampleClasses[0],
        dayName = "Monday",
        status = ClassStatus.ONGOING,
        remainingFormatted = "00:45:12",
        progressPercent = 0.5f,
        hasClassesToday = true
    )

    MaterialTheme {
        NowTabScreen(
            status = sampleStatus,
            weekSchedule = WeekSchedule(sampleDays),
            selectedDate = LocalDate.now(),
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            selectedDay = "Monday",
            onDateSelected = { _, _ -> },
            onPreviousWeek = {},
            onNextWeek = {},
            onTodayClick = {}
        )
    }
}
