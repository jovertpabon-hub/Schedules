package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmCoffeeCream
import com.example.schedules.ui.theme.WarmRoast
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

data class VacantTimeBlock(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long
)

/**
 * Merges overlapping or contiguous class time intervals into a list of non-overlapping time spans.
 * This ensures clock time is counted correctly and not double-counted when classes overlap.
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
            // Overlapping or back-to-back: extend current interval
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

/**
 * Calculates total clock time occupied by classes.
 * Overlapping class periods are merged into single intervals so clock time is counted only once,
 * keeping class duration consistent with free time gap calculations.
 */
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

/**
 * Calculates vacant time blocks (free time gaps) within an active day window.
 * The default window is 8:00 AM to 5:00 PM (17:00). If classes start earlier or end later,
 * the window is expanded to accommodate them.
 * Overlapping classes are merged first to avoid false gaps or negative durations.
 */
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
    selectedDay: String,
    onSelectedDayChanged: (String) -> Unit,
    onEditClass: (CourseClass, String) -> Unit = { _, _ -> },
    onDeleteClass: (CourseClass) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    // Live time & date ticker so UI stays updated without heavy recomposition overhead
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
            delay(1.seconds)
        }
    }

    val todayName = remember(currentDate) {
        val name = currentDate.dayOfWeek.name
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(name, ignoreCase = true) } ?: "Monday"
    }

    val currentHour = currentTime.hour
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning! ☀️"
            in 12..17 -> "Midday boost! ☕"
            else -> "Evening unwind! 🌙"
        }
    }
    var showVacantTimes by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmCoffeeCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header with Greeting and Date
            NowTabHeader(
                currentDate = currentDate,
                greeting = greeting
            )

            // 1b. Primary Navigation: Horizontally scrolling Day Selection Chips
            DaySelectionChipsBar(
                selectedDay = selectedDay,
                todayName = todayName,
                weekSchedule = weekSchedule,
                onSelectedDayChanged = { day ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectedDayChanged(day)
                }
            )

            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = {
                    val daysList = ScheduleConstants.DaysOfWeek
                    val oldIndex = daysList.indexOfFirst { it.equals(initialState, ignoreCase = true) }
                    val newIndex = daysList.indexOfFirst { it.equals(targetState, ignoreCase = true) }
                    val isForward = !(oldIndex != -1 && newIndex != -1) || newIndex > oldIndex

                    if (isForward) {
                        (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (width * 0.3f).toInt() } + fadeIn(animationSpec = tween(250)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { width -> (-width * 0.3f).toInt() } + fadeOut(animationSpec = tween(200)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (-width * 0.3f).toInt() } + fadeIn(animationSpec = tween(250)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { width -> (width * 0.3f).toInt() } + fadeOut(animationSpec = tween(200)))
                    }.using(SizeTransform(clip = false))
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
                val isTodayCurrent = currentDay.equals(todayName, ignoreCase = true)
                val currentVacantBlocks = remember(currentDayData.classes) {
                    calculateVacantTimeBlocks(currentDayData.classes)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 2. HERO COUNTDOWN CARD (TODAY) OR DAY OVERVIEW HERO (OTHER DAYS)
                    if (isTodayCurrent) {
                        NowTabCountdownCard(
                            status = status
                        )
                    } else {
                        DaySummaryHeroCard(
                            dayData = currentDayData
                        )
                    }

                    // 3. CLASS SCHEDULE OR FREE TIME LIST (PRIMARY CONTENT)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Section Header & View Toggle Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showVacantTimes) {
                                    "${currentDay.uppercase(Locale.US)} FREE TIME"
                                } else {
                                    "${currentDay.uppercase(Locale.US)} CLASSES"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkMocha,
                                letterSpacing = 0.6.sp
                            )

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showVacantTimes = !showVacantTimes
                                },
                                shape = RoundedCornerShape(50.dp),
                                color = if (showVacantTimes) DeepEspresso else SteamedMilk,
                                border = BorderStroke(1.dp, DarkMocha.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Toggle View",
                                        tint = if (showVacantTimes) SteamedMilk else DeepEspresso,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (showVacantTimes) "Show Classes" else "Free Time",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showVacantTimes) SteamedMilk else DeepEspresso
                                    )
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
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

                    // 4. DAILY SUMMARY DASHBOARD (STATISTICS & TIMELINE - SUPPORTING OVERVIEW)
                    if (showVacantTimes) {
                        DailyOverviewDashboard(
                            dayData = currentDayData,
                            vacantBlocks = currentVacantBlocks,
                            currentTime = currentTime,
                            isToday = isTodayCurrent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowTabHeader(
    currentDate: LocalDate,
    greeting: String
) {
    val formattedDate = remember(currentDate) {
        currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))
    }
    val textColor = DarkMocha

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = textColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor.copy(alpha = 0.8f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun DaySelectionChipsBar(
    selectedDay: String,
    todayName: String,
    weekSchedule: WeekSchedule,
    onSelectedDayChanged: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ScheduleConstants.DaysOfWeek) { day ->
            val isSelected = day.equals(selectedDay, ignoreCase = true)
            val isToday = day.equals(todayName, ignoreCase = true)
            val dayData = weekSchedule.days.firstOrNull { it.dayName.equals(day, ignoreCase = true) }
            val classCount = dayData?.classes?.size ?: 0

            val chipBg = when {
                isSelected -> DeepEspresso
                else -> SteamedMilk
            }
            val textColor = when {
                isSelected -> GoldenCrema
                else -> DarkMocha
            }
            val border = when {
                isSelected -> BorderStroke(1.5.dp, GoldenCrema)
                else -> BorderStroke(1.dp, DarkMocha.copy(alpha = 0.15f))
            }

            Surface(
                onClick = { onSelectedDayChanged(day) },
                shape = RoundedCornerShape(14.dp),
                color = chipBg,
                border = border,
                shadowElevation = if (isSelected) 3.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = day.take(3).uppercase(Locale.US),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = textColor
                    )

                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldenCrema else WarmRoast)
                        )
                    }

                    Surface(
                        color = if (isSelected) GoldenCrema.copy(alpha = 0.25f) else DarkMocha.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = when (classCount) {
                                0 -> "Free"
                                else -> "$classCount"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
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
            .border(
                1.5.dp,
                GoldenCrema.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${dayData.dayName.uppercase(Locale.US)} OVERVIEW",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SteamedMilk,
                letterSpacing = 0.8.sp
            )

            if (dayData.classes.isNotEmpty()) {
                val firstStart = dayData.classes.minOf { it.startTime }.format(ScheduleConstants.TimeFormatter)
                val lastEnd = dayData.classes.maxOf { it.endTime }.format(ScheduleConstants.TimeFormatter)

                Text(
                    text = "$firstStart - $lastEnd",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = GoldenCrema
                )
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
        // HEADER: STATUS TITLE
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
                        ClassStatus.NONE -> if (status.hasClassesToday) Icons.Default.CheckCircle else Icons.Default.FreeBreakfast
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
            // Clean empty state distinguishing no-classes-today vs all-classes-completed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = GoldenCrema.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FreeBreakfast,
                            contentDescription = null,
                            tint = GoldenCrema,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (status.hasClassesToday) "All Classes Finished" else "No Classes Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk
                    )
                    Text(
                        text = if (status.hasClassesToday) "All scheduled lectures are completed. Enjoy your time! ☕" else "No lectures scheduled for today. Enjoy your free day! ☕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteamedMilk.copy(alpha = 0.85f)
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
                // LEFT: TIMER DISPLAY (Circular progress ring for ONGOING; subtle border ring for UPCOMING)
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

                // RIGHT: CLASS DETAILS
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Course Name
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Start - End Time
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

                    // Location / Room
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
private fun DailyOverviewDashboard(
    dayData: DayScheduleData,
    vacantBlocks: List<VacantTimeBlock>,
    currentTime: LocalTime,
    isToday: Boolean
) {
    val totalClassTime = remember(dayData.classes) { calculateTotalClassHours(dayData.classes) }
    val totalFreeTime = remember(vacantBlocks) { calculateTotalFreeTime(vacantBlocks) }

    val cardBg = SteamedMilk
    val titleColor = DeepEspresso

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DAILY SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor,
                letterSpacing = 0.6.sp
            )

            // 3 key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticItem(
                    label = "Classes",
                    value = "${dayData.classes.size}",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    label = "Class Time",
                    value = totalClassTime,
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    label = "Free Time",
                    value = totalFreeTime,
                    icon = Icons.Default.FreeBreakfast,
                    modifier = Modifier.weight(1f)
                )
            }

            // Timeline
            if (dayData.classes.isNotEmpty()) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = DarkMocha.copy(alpha = 0.12f)
                )

                val sortedClasses = dayData.classes.sortedBy { it.startTime }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sortedClasses.forEachIndexed { index, course ->
                        val isCompleted = isToday && !currentTime.isBefore(course.endTime)
                        val isOngoing = isToday && !currentTime.isBefore(course.startTime) && currentTime.isBefore(course.endTime)
                        
                        val indicatorColor = when {
                            isToday && isOngoing -> DeepEspresso
                            isToday && isCompleted -> WarmRoast.copy(alpha = 0.5f)
                            else -> DarkMocha.copy(alpha = 0.35f)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.widthIn(min = 60.dp)
                            ) {
                                Surface(
                                    color = indicatorColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                                Text(
                                    text = course.courseCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkMocha,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = course.startTime.format(ScheduleConstants.TimeFormatter),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkMocha.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (index < sortedClasses.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(1.5.dp)
                                        .background(DarkMocha.copy(alpha = 0.15f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WarmCoffeeCream,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, DarkMocha.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepEspresso,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DarkMocha,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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

    val cardBg = SteamedMilk

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = GoldenCrema,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FreeBreakfast,
                            contentDescription = "Free time",
                            tint = DeepEspresso,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Gap duration",
                        tint = DarkMocha,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${block.startTime.format(ScheduleConstants.TimeFormatter)} - ${block.endTime.format(ScheduleConstants.TimeFormatter)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkMocha
                    )
                }
            }

            Surface(
                color = DeepEspresso,
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
        isCompleted -> DarkMocha.copy(alpha = 0.6f)
        else -> DarkMocha
    }
    val iconTint = DarkMocha.copy(alpha = 0.7f)

    val cardBg = when {
        isCompleted -> SteamedMilk.copy(alpha = 0.75f)
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
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Subtle left edge accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(
                        when {
                            isOngoing -> DeepEspresso
                            isCompleted -> WarmRoast.copy(alpha = 0.4f)
                            else -> GoldenCrema
                        }
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Row: Code + Name + More Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (isOngoing) DeepEspresso else WarmRoast,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SteamedMilk,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
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

                // Middle Row: Time Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                // Bottom Row: Room Location & Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (course.room.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false)
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
                                fontWeight = FontWeight.Bold,
                                color = cardTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
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
            selectedDay = "Monday",
            onSelectedDayChanged = {}
        )
    }
}
