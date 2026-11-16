package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.now.components.ClassesFreeTimeSelector
import com.example.schedules.ui.now.components.NowTabClassDetails
import com.example.schedules.ui.now.components.NowTabCountdownCard
import com.example.schedules.ui.now.components.VacantTimeBlockCard
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

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

/**
 * Unified Now and Schedule Screen for Clofee.
 * Contains:
 * - Clofee Header with title, current date, Add Class, Search, and overflow actions.
 * - Swipeable Overview Section (Page 1: Timer Overview, Page 2: Calendar Overview).
 * - Subtle page indicator.
 * - Single Shared Schedule Section below the pager.
 */
@Composable
fun NowTabScreen(
    status: ScheduleStatus,
    weekSchedule: WeekSchedule,
    selectedDate: LocalDate = remember { LocalDate.now() },
    weekStart: LocalDate = remember(selectedDate) {
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    },
    currentTime: LocalTime = LocalTime.now(),
    notesMap: Map<Long, String> = emptyMap(),
    notesDates: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate, String) -> Unit = { _, _ -> },
    onTodayClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onEditClass: (CourseClass, String) -> Unit = { _, _ -> },
    onDeleteClass: (CourseClass) -> Unit = {},
    onAddClass: () -> Unit = {},
    onImportPdf: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onSaveNote: (Long, String) -> Unit = { _, _ -> },
    onDeleteNote: (Long) -> Unit = {},
    initialPage: Int = 0,
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var showVacantTimes by rememberSaveable { mutableStateOf(false) }

    val todayDate = remember { LocalDate.now() }
    val isToday = selectedDate == todayDate

    val selectedDay = remember(selectedDate) {
        val name = selectedDate.dayOfWeek.name
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(name, ignoreCase = true) } ?: "Sunday"
    }

    val selectedDayData = remember(weekSchedule, selectedDay) {
        val daySchedule = weekSchedule.days.firstOrNull { it.dayName.equals(selectedDay, ignoreCase = true) }
        DayScheduleData(
            dayName = selectedDay,
            classes = daySchedule?.classes?.sortedBy { it.startTime } ?: emptyList()
        )
    }

    val vacantBlocks = remember(selectedDayData.classes) {
        com.example.schedules.ui.now.utils.calculateVacantTimeBlocks(selectedDayData.classes)
    }

    val classCount = selectedDayData.classes.size

    val formattedSelectedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM d y", Locale.US))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. ParClock Header Section (Fixed at top)
            NowTabHeaderSection(
                onAddClass = onAddClass,
                onImportPdf = onImportPdf,
                onClearAll = onClearAll,
                onSettingsClick = onSettingsClick,
                onSearchClick = onSearchClick,
                onAboutClick = onAboutClick
            )

            // 2. Scrollable Content: Overview Pager + Page Indicator + Shared Schedule Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section 1: Overview Group (Pager + Indicator)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        pageSpacing = 16.dp,
                        beyondViewportPageCount = 1,
                        verticalAlignment = Alignment.Top
                    ) { page ->

                        when (page) {
                            0 -> {
                                // Timer Overview
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    NowTabCountdownCard(
                                        status = status,
                                        selectedDate = selectedDate,
                                        onClick = onTodayClick
                                    )
                                }
                            }

                            1 -> {
                                // Calendar Overview
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 1.dp
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.25.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                                                RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        WeeklyCalendarStrip(
                                            weekStart = weekStart,
                                            selectedDate = selectedDate,
                                            weekSchedule = weekSchedule,
                                            notesDates = notesDates,
                                            onDateSelected = onDateSelected,
                                            onTodayClick = onTodayClick,
                                            onCalendarClick = onCalendarClick
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Page Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 22.dp else 8.dp,
                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                label = "indicatorWidth"
                            )
                            val color by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                animationSpec = tween(durationMillis = 250),
                                label = "indicatorColor"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }
                }

                // 3. Shared Schedule Section (Updates with selectedDate without resetting on swipe)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isToday) "Today's Schedule" else "$selectedDay's Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formattedSelectedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    ) {
                        Text(
                            text = if (showVacantTimes) {
                                "${vacantBlocks.size} ${if (vacantBlocks.size == 1) "Gap" else "Gaps"}"
                            } else {
                                "$classCount ${if (classCount == 1) "Class" else "Classes"}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                        )
                    }
                }

                // Classes / Free Time Selector
                ClassesFreeTimeSelector(
                    showVacantTimes = showVacantTimes,
                    onSelectionChanged = { showVacantTimes = it }
                )

                // Schedule Cards or Free Time Content
                AnimatedContent(
                    targetState = selectedDate,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing))
                            .togetherWith(fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing)))
                    },
                    label = "DayContentTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { targetDate ->
                    val isTargetToday = targetDate == todayDate
                    if (showVacantTimes) {
                        if (vacantBlocks.isEmpty()) {
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
                                vacantBlocks.forEach { block ->
                                    VacantTimeBlockCard(block = block)
                                }
                            }
                        }
                    } else {
                        if (selectedDayData.classes.isEmpty()) {
                            EmptyDayView(
                                modifier = Modifier.fillMaxWidth(),
                                mainMessage = if (isTargetToday) "No classes scheduled for today!" else "No classes for $selectedDay!",
                                secondaryMessage = "Enjoy a free day"
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                selectedDayData.classes.forEach { course ->
                                    val courseId = course.id
                                    val note = courseId?.let { notesMap[it] }

                                    NowTabClassDetails(
                                        course = course,
                                        isToday = isTargetToday,
                                        currentTime = currentTime,
                                        selectedDate = targetDate,
                                        note = note,
                                        onEdit = {
                                            onEditClass(course, selectedDay)
                                        },
                                        onDelete = {
                                            onDeleteClass(course)
                                        },
                                        onSaveNote = { text ->
                                            courseId?.let {
                                                onSaveNote(it, text)
                                            }
                                        },
                                        onDeleteNote = {
                                            courseId?.let {
                                                onDeleteNote(it)
                                            }
                                        }
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
private fun NowTabHeaderSection(
    onAddClass: () -> Unit,
    onImportPdf: () -> Unit,
    onClearAll: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {
                NowTabHeader(
                    onAddClass = onAddClass,
                    onImportPdf = onImportPdf,
                    onClearAll = onClearAll,
                    onSettingsClick = onSettingsClick,
                    onSearchClick = onSearchClick,
                    onAboutClick = onAboutClick
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun NowTabHeader(
    onAddClass: () -> Unit = {},
    onImportPdf: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showHeaderMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAboutClick()
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "parclock",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 28.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-0.5).sp,
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                            offset = Offset(1f, 2f),
                            blurRadius = 3f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "WHAT'S ON YOUR DAY?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddClass()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Class",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSearchClick()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Box {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showHeaderMenu = true
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showHeaderMenu,
                    onDismissRequest = { showHeaderMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Import COE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {
                            showHeaderMenu = false
                            onImportPdf()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear All", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showHeaderMenu = false
                            onClearAll()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {
                            showHeaderMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    weekSchedule: WeekSchedule,
    notesDates: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate, String) -> Unit,
    onTodayClick: () -> Unit = {
        val today = LocalDate.now()
        val dayName = ScheduleConstants.DaysOfWeek.firstOrNull {
            it.equals(today.dayOfWeek.name, ignoreCase = true)
        } ?: "Sunday"
        onDateSelected(today, dayName)
    },
    onCalendarClick: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WeeklyCalendarHeader(
            selectedDate = selectedDate,
            onTodayClick = {
                if (selectedDate != today) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTodayClick()
                }
            },
            onCalendarClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCalendarClick()
            }
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        WeeklyDateSelector(
            weekStart = weekStart,
            selectedDate = selectedDate,
            weekSchedule = weekSchedule,
            notesDates = notesDates,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun WeeklyCalendarHeader(
    selectedDate: LocalDate,
    onTodayClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val monthYearText = remember(selectedDate) {
        selectedDate.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
        )
    }

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Open calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedContent(
                    targetState = monthYearText,
                    label = "monthYearTransition"
                ) { targetText ->
                    Text(
                        text = targetText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            TextButton(
                onClick = onTodayClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
            ) {
                Text(
                    text = "Today",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WeeklyDateSelector(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    weekSchedule: WeekSchedule,
    notesDates: Set<LocalDate>,
    onDateSelected: (LocalDate, String) -> Unit
) {
    val today = remember { LocalDate.now() }

    AnimatedContent(
        targetState = weekStart,
        transitionSpec = {
            if (targetState.isAfter(initialState)) {
                (slideInHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> width / 3 } +
                 fadeIn(animationSpec = tween(140)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                        fadeOut(animationSpec = tween(120))
                    )
            } else {
                (slideInHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                 fadeIn(animationSpec = tween(140)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width -> width / 3 } +
                        fadeOut(animationSpec = tween(120))
                    )
            }
        },
        label = "weekDatesTransition"
    ) { currentWeekStart ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScheduleConstants.DaysOfWeek.forEachIndexed { index, dayName ->
                val dayDate = currentWeekStart.plusDays(index.toLong())
                val isSelectedDate = dayDate == selectedDate
                val isToday = dayDate == today

                val dayData = weekSchedule.days.firstOrNull { it.dayName.equals(dayName, ignoreCase = true) }
                val classCount = dayData?.classes?.size ?: 0
                val hasClasses = classCount > 0
                val hasNote = notesDates.contains(dayDate)

                WeeklyDateTile(
                    modifier = Modifier.weight(1f),
                    dayName = dayName,
                    dayDate = dayDate,
                    isSelected = isSelectedDate,
                    isToday = isToday,
                    hasClasses = hasClasses,
                    hasNote = hasNote,
                    onSelect = { onDateSelected(dayDate, dayName) }
                )
            }
        }
    }
}

@Composable
private fun WeeklyDateTile(
    modifier: Modifier = Modifier,
    dayName: String,
    dayDate: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasClasses: Boolean,
    hasNote: Boolean,
    onSelect: () -> Unit
) {
    val weekdayAbbrev = dayName.take(3).uppercase(Locale.US)
    val dateNumber = dayDate.dayOfMonth.toString()
    val haptic = LocalHapticFeedback.current

    val tileBgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(150),
        label = "tileBgColor"
    )

    val weekdayTextColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    }

    val dateTextColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSelect()
        },
        shape = RoundedCornerShape(14.dp),
        color = tileBgColor,
        border = when {
            isSelected -> BorderStroke(1.25.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
            isToday -> BorderStroke(1.25.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        },
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = 11.dp,
                horizontal = 2.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = weekdayAbbrev,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = weekdayTextColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = dateNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = dateTextColor,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasClasses) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                    if (hasNote) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primaryContainer
                                )
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

    val today = remember { LocalDate.now() }
    val todayName = remember(today) {
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(today.dayOfWeek.name, ignoreCase = true) } ?: "Sunday"
    }

    val sampleDays = ScheduleConstants.DaysOfWeek.map { dayName ->
        DayScheduleData(dayName = dayName, classes = if (dayName == todayName) sampleClasses else emptyList())
    }

    val sampleStatus = ScheduleStatus(
        courseClass = sampleClasses[0],
        dayName = todayName,
        status = ClassStatus.ONGOING,
        remainingFormatted = "00:45:12",
        progressPercent = 0.5f,
        hasClassesToday = true
    )

    MaterialTheme {
        NowTabScreen(
            status = sampleStatus,
            weekSchedule = WeekSchedule(sampleDays)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NowTabCalendarPreview() {
    val sampleClasses = listOf(
        CourseClass(id = 1, courseCode = "CS101", courseName = "Intro to Computer Science", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30), room = "Room 302")
    )
    val today = remember { LocalDate.now() }
    val todayName = remember(today) {
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(today.dayOfWeek.name, ignoreCase = true) } ?: "Sunday"
    }
    val sampleDays = ScheduleConstants.DaysOfWeek.map { dayName ->
        DayScheduleData(dayName = dayName, classes = if (dayName == todayName) sampleClasses else emptyList())
    }
    val sampleStatus = ScheduleStatus(
        courseClass = sampleClasses[0],
        dayName = todayName,
        status = ClassStatus.ONGOING,
        remainingFormatted = "00:45:12",
        progressPercent = 0.5f,
        hasClassesToday = true
    )
    MaterialTheme {
        NowTabScreen(
            status = sampleStatus,
            weekSchedule = WeekSchedule(sampleDays),
            selectedDate = today.plusDays(1),
            initialPage = 1
        )
    }
}

