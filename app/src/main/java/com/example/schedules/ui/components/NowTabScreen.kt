package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.R
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
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

data class VacantTimeBlock(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Long
)

fun calculateVacantTimeBlocks(classes: List<CourseClass>): List<VacantTimeBlock> {
    val windowStart = LocalTime.of(8, 0)
    val windowEnd = LocalTime.of(17, 0)
    val vacantBlocks = mutableListOf<VacantTimeBlock>()

    if (classes.isEmpty()) {
        val duration = Duration.between(windowStart, windowEnd).toMinutes()
        return listOf(VacantTimeBlock(windowStart, windowEnd, duration))
    }

    var currentPointer = windowStart

    for ((_, _, _, startTime, endTime) in classes.sortedBy { it.startTime }) {
        val courseStart = if (startTime.isBefore(windowStart)) windowStart else startTime
        val courseEnd = if (endTime.isAfter(windowEnd)) windowEnd else endTime

        if (courseStart.isAfter(windowEnd)) break

        if (courseStart.isAfter(currentPointer)) {
            val gapMinutes = Duration.between(currentPointer, courseStart).toMinutes()
            if (gapMinutes > 0) {
                vacantBlocks.add(VacantTimeBlock(currentPointer, courseStart, gapMinutes))
            }
        }

        if (courseEnd.isAfter(currentPointer)) {
            currentPointer = courseEnd
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

fun calculateTotalClassHours(classes: List<CourseClass>): String {
    val totalMinutes = classes.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0 && mins > 0) "$hours hrs $mins m"
    else if (hours > 0) "$hours hrs"
    else "$mins mins"
}

fun calculateTotalFreeTime(vacantBlocks: List<VacantTimeBlock>): String {
    val totalMinutes = vacantBlocks.sumOf { it.durationMinutes }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0 && mins > 0) "$hours hrs $mins m"
    else if (hours > 0) "$hours hrs"
    else "$mins mins"
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
    val todayName = remember {
        val name = java.time.LocalDate.now().dayOfWeek.name
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(name, ignoreCase = true) } ?: "Monday"
    }

    val currentHour = remember { LocalTime.now().hour }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning! ☀️"
            in 12..17 -> "Midday boost! ☕"
            else -> "Evening unwind! 🌙"
        }
    }
    val isEveningUnwind = greeting == "Evening unwind! 🌙"
    val backgroundDrawable = if (isEveningUnwind) R.drawable.dark_mode_bg else R.drawable.bg_6

    var isDetailsExpanded by remember { mutableStateOf(true) }
    var showVacantTimes by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = backgroundDrawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NowTabHeader(
                selectedDay = selectedDay,
                weekSchedule = weekSchedule,
                onSelectedDayChanged = onSelectedDayChanged,
                isEveningUnwind = isEveningUnwind
            )

            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = {
                    val daysList = ScheduleConstants.DaysOfWeek
                    val oldIndex = daysList.indexOfFirst { it.equals(initialState, ignoreCase = true) }
                    val newIndex = daysList.indexOfFirst { it.equals(targetState, ignoreCase = true) }
                    val isForward = !(oldIndex != -1 && newIndex != -1) || newIndex > oldIndex

                    if (isForward) {
                        (slideInHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> (width * 0.35f).toInt() } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (-width * 0.35f).toInt() } + fadeOut(animationSpec = tween(250)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> (-width * 0.35f).toInt() } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> (width * 0.35f).toInt() } + fadeOut(animationSpec = tween(250)))
                    }.using(SizeTransform(clip = false))
                },
                label = "DayContentTransition",
                modifier = Modifier.fillMaxWidth()
            ) { currentDay ->
                val currentDayData = remember(weekSchedule, currentDay) {
                    weekSchedule.days.firstOrNull { it.dayName.equals(currentDay, ignoreCase = true) }
                        ?: DayScheduleData(currentDay, emptyList())
                }
                val isTodayCurrent = currentDay.equals(todayName, ignoreCase = true)
                val currentVacantBlocks = remember(currentDayData.classes) {
                    calculateVacantTimeBlocks(currentDayData.classes)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // MAIN OVERVIEW BOX
                    if (isTodayCurrent) {
                        NowTabCountdownCard(
                            status = status
                        )
                    } else {
                        DaySummaryHeroCard(
                            dayData = currentDayData
                        )
                    }

                    // COMPACT DAILY OVERVIEW DASHBOARD (APPEARS ONLY WHEN DETAILED SCHEDULE IS COLLAPSED)
                    AnimatedVisibility(
                        visible = !isDetailsExpanded,
                        enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + 
                                slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -25 },
                        exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                               slideOutVertically(animationSpec = tween(200)) { -25 }
                    ) {
                        DailyOverviewDashboard(
                            dayData = currentDayData,
                            vacantBlocks = currentVacantBlocks,
                            status = status,
                            isEveningUnwind = isEveningUnwind
                        )
                    }

                    // DETAILED SCHEDULE LIST OR VACANT TIMES BREAKDOWN
                    AnimatedVisibility(
                        visible = isDetailsExpanded,
                        enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + 
                                slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { 25 },
                        exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                               slideOutVertically(animationSpec = tween(200)) { 25 }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // On/Off Toggle Icon Button for Free Time
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
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
                                    color = if (isEveningUnwind) SteamedMilk else DarkMocha,
                                    letterSpacing = 0.8.sp
                                )

                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showVacantTimes = !showVacantTimes
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    color = if (showVacantTimes) DeepEspresso else SteamedMilk,
                                    border = BorderStroke(1.dp, if (isEveningUnwind) GoldenCrema.copy(alpha = 0.4f) else DarkMocha.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Toggle Free Time filter",
                                            tint = if (showVacantTimes) SteamedMilk else DeepEspresso,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Free Time",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showVacantTimes) SteamedMilk else DeepEspresso
                                        )
                                    }
                                }
                            }

                            if (showVacantTimes) {
                                // VACANT TIME BLOCKS VIEW
                                if (currentVacantBlocks.isEmpty()) {
                                    EmptyDayView(
                                        modifier = Modifier.fillMaxWidth(),
                                        mainMessage = "Fully booked day!",
                                        secondaryMessage = "No vacant time blocks between 8:00 AM and 5:00 PM"
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        currentVacantBlocks.forEach { block ->
                                            VacantTimeBlockCard(block = block)
                                        }
                                    }
                                }
                            } else {
                                // BUSY CLASSES VIEW
                                if (currentDayData.classes.isEmpty()) {
                                    EmptyDayView(
                                        modifier = Modifier.fillMaxWidth(),
                                        mainMessage = "No classes for $currentDay!",
                                        secondaryMessage = "Enjoy a free day or catch up on studies"
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        currentDayData.classes.forEach { course ->
                                            NowTabClassDetails(
                                                course = course,
                                                isToday = isTodayCurrent,
                                                isEveningUnwind = isEveningUnwind,
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
}

@Composable
private fun DailyOverviewDashboard(
    dayData: DayScheduleData,
    vacantBlocks: List<VacantTimeBlock>,
    status: ScheduleStatus,
    isEveningUnwind: Boolean
) {
    val totalClassTime = remember(dayData.classes) { calculateTotalClassHours(dayData.classes) }
    val totalFreeTime = remember(vacantBlocks) { calculateTotalFreeTime(vacantBlocks) }

    val contextualMessage = remember(dayData.classes, status) {
        when {
            dayData.classes.isEmpty() -> "Enjoy your free day! No classes scheduled ☕"
            status.status == ClassStatus.ONGOING -> "Currently in class: ${status.courseClass?.courseName ?: "Class"}"
            status.status == ClassStatus.UPCOMING -> "Next class: ${status.courseClass?.courseName ?: "Class"} at ${status.courseClass?.startTime?.format(ScheduleConstants.TimeFormatter)}"
            else -> "All scheduled classes completed for today!"
        }
    }

    val cardBg = if (isEveningUnwind) Color(0xFF1E140F) else SteamedMilk
    val titleColor = if (isEveningUnwind) GoldenCrema else DeepEspresso

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isEveningUnwind) GoldenCrema.copy(alpha = 0.5f) else GoldenCrema, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY SUMMARY DASHBOARD",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    letterSpacing = 0.8.sp
                )
            }

            // Daily statistics (3 metrics) with flexible sizing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            HorizontalDivider(
                thickness = 0.8.dp,
                color = DarkMocha.copy(alpha = 0.15f)
            )

            // Horizontal Class Timeline
            if (dayData.classes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CLASS TIMELINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkMocha.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sortedClasses = dayData.classes.sortedBy { it.startTime }
                        val now = LocalTime.now()

                        sortedClasses.forEachIndexed { index, course ->
                            val isCompleted = course.endTime.isBefore(now)
                            val isOngoing = !isCompleted && !course.startTime.isAfter(now)
                            val indicatorColor = when {
                                isOngoing -> DeepEspresso
                                isCompleted -> WarmRoast.copy(alpha = 0.6f)
                                else -> GoldenCrema
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = indicatorColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(12.dp),
                                    border = BorderStroke(1.dp, DarkMocha.copy(alpha = 0.3f))
                                ) {}
                                Text(
                                    text = course.courseCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkMocha,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = course.startTime.format(ScheduleConstants.TimeFormatter),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = if (isEveningUnwind) SteamedMilk else DarkMocha.copy(alpha = 0.6f)
                                )
                            }

                            if (index < sortedClasses.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .height(2.dp)
                                        .background(DarkMocha.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = DarkMocha.copy(alpha = 0.15f)
                )
            }

            // Contextual Status Message
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = GoldenCrema,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = DeepEspresso,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = contextualMessage,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = DarkMocha.copy(alpha = 0.9f)
                )
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
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, DarkMocha.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepEspresso,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha.copy(alpha = 0.75f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = DarkMocha,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VacantTimeBlockCard(block: VacantTimeBlock) {
    val hours = block.durationMinutes / 60
    val mins = block.durationMinutes % 60
    val durationText = when {
        hours > 0 && mins > 0 -> "$hours hr $mins mins free"
        hours > 0 -> "$hours hr free"
        else -> "$mins mins free"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SteamedMilk
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                GoldenCrema,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = GoldenCrema,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FreeBreakfast,
                            contentDescription = null,
                            tint = DeepEspresso,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = DarkMocha,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${block.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                                block.endTime.format(ScheduleConstants.TimeFormatter)
                            }",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkMocha
                        )
                    }
                    Text(
                        text = "Free gap window • Great time for coffee break ☕",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkMocha.copy(alpha = 0.75f)
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
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// NOW TAB HEADER (PRESERVED UNCHANGED AS REQUESTED)
// -----------------------------------------------------------------------------
@Composable
private fun NowTabHeader(
    selectedDay: String,
    weekSchedule: WeekSchedule,
    onSelectedDayChanged: (String) -> Unit,
    isEveningUnwind: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val currentHour = remember { LocalTime.now().hour }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning! ☀️"
            in 12..17 -> "Midday boost! ☕"
            else -> "Evening unwind! 🌙"
        }
    }

    val todayName = remember {
        val name = java.time.LocalDate.now().dayOfWeek.name
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(name, ignoreCase = true) } ?: "Monday"
    }

    val isToday = selectedDay.equals(todayName, ignoreCase = true)
    val textColor = if (isEveningUnwind) SteamedMilk else DarkMocha
    val surfaceColor = if (isEveningUnwind) Color(0xFF1E140F) else SteamedMilk
    val surfaceBorder = if (isEveningUnwind) GoldenCrema.copy(alpha = 0.5f) else DarkMocha.copy(alpha = 0.4f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isToday) "TODAY'S SCHEDULE" else "${selectedDay.uppercase(Locale.US)} SCHEDULE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp
            )
        }

        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(50.dp),
                color = surfaceColor,
                border = BorderStroke(1.dp, surfaceBorder),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_coffee),
                        contentDescription = "Select Day",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isToday) "Today" else selectedDay,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Menu",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(surfaceColor)
                    .border(
                        width = 1.dp,
                        color = GoldenCrema
                    )

            ) {
                ScheduleConstants.DaysOfWeek.forEach { day ->
                    val dayData = weekSchedule.days.firstOrNull { it.dayName.equals(day, ignoreCase = true) }
                    val classCount = dayData?.classes?.size ?: 0
                    val isDayToday = day.equals(todayName, ignoreCase = true)
                    val isDaySelected = day.equals(selectedDay, ignoreCase = true)

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDayToday) "$day (Today)" else day,
                                    fontWeight = if (isDaySelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Surface(
                                    color = if (isDaySelected) DeepEspresso else GoldenCrema,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        text = when (classCount) {
                                            0 -> "Free"
                                            1 -> "1 class"
                                            else -> "$classCount classes"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDaySelected) SteamedMilk else DeepEspresso,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectedDayChanged(day)
                            expanded = false
                        }
                    )
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
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                GoldenCrema.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.card_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${dayData.dayName.uppercase(Locale.US)} OVERVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteamedMilk,
                    letterSpacing = 0.8.sp
                )

                if (dayData.classes.isNotEmpty()) {
                    val firstStart =
                        dayData.classes.first().startTime.format(ScheduleConstants.TimeFormatter)
                    val lastEnd =
                        dayData.classes.last().endTime.format(ScheduleConstants.TimeFormatter)

                    Text(
                        text = "$firstStart - $lastEnd",
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.headlineSmall,
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

                CardInstructionText(
                    text = "Chronological Daily Overview",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    textColor = SteamedMilk.copy(alpha = 0.8f)
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
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                GoldenCrema.copy(alpha = 0.6f),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.card_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            CountdownTimerView(status = status)
        }
    }
}

@Composable
private fun CountdownTimerView(
    status: ScheduleStatus
) {
    val textColorPrimary = SteamedMilk
    val textColorAccent = GoldenCrema

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 18.dp,
                vertical = 22.dp
            ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // HEADER
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
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = textColorPrimary,
                    modifier = Modifier.size(15.dp)
                )

                Text(
                    text = when (status.status) {
                        ClassStatus.ONGOING -> "TIME REMAINING"
                        ClassStatus.UPCOMING -> "NEXT CLASS"
                        ClassStatus.NONE -> "TODAY'S OVERVIEW"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColorPrimary,
                    letterSpacing = 0.6.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = GoldenCrema
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (status.status == ClassStatus.ONGOING)
                                    Color(0xFFE88900)
                                else DeepEspresso
                            )
                    )

                    Text(
                        text = when (status.status) {
                            ClassStatus.ONGOING -> "IN PROGRESS"
                            ClassStatus.UPCOMING -> "UPCOMING"
                            ClassStatus.NONE -> "ALL DONE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepEspresso
                    )
                }
            }
        }

        // MAIN HORIZONTAL LAYOUT
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val timerSize = if (maxWidth < 340.dp) 105.dp else 120.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // LEFT: CIRCULAR COUNTDOWN
                Box(
                    modifier = Modifier.size(timerSize),
                    contentAlignment = Alignment.Center
                ) {

                    if (status.status == ClassStatus.ONGOING) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val strokeWidth = 9.dp.toPx()

                            drawArc(
                                color = Color(0xFF79552D)
                                    .copy(alpha = 0.5f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )

                            drawArc(
                                color = textColorAccent,
                                startAngle = -90f,
                                sweepAngle = 360f *
                                        status.progressPercent.coerceIn(0f, 1f),
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SteamedMilk.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = if (status.status == ClassStatus.NONE)
                                "All Done!"
                            else status.remainingFormatted,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = SteamedMilk,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Text(
                            text = "HRS  MIN  SEC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteamedMilk.copy(alpha = 0.65f)
                        )
                    }
                }
                // RIGHT: CLASS INFORMATION
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CLASS DETAILS
                    status.courseClass?.let { course ->

                        Text(
                            text = when (status.status) {
                                ClassStatus.ONGOING -> "CURRENT CLASS"
                                ClassStatus.UPCOMING -> "UPCOMING CLASS"
                                ClassStatus.NONE -> "CLASS"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = GoldenCrema
                        )

                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = SteamedMilk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${
                                course.startTime.format(
                                    ScheduleConstants.TimeFormatter
                                )
                            } - ${
                                course.endTime.format(
                                    ScheduleConstants.TimeFormatter
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = SteamedMilk.copy(alpha = 0.75f)
                        )
                    }

                    if (status.status == ClassStatus.NONE) {
                        Text(
                            text = "No more classes",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteamedMilk.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // BOTTOM SCHEDULE PILL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color(0xFF4B2605).copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    SteamedMilk.copy(alpha = 0.25f)
                )
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = SteamedMilk,
                        modifier = Modifier.size(13.dp)
                    )

                    Text(
                        text = "Unified Chronological Schedule",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk
                    )
                }
            }
        }
    }
}

@Composable
private fun CardInstructionText(text: String, icon: ImageVector, textColor: Color = DarkMocha) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = textColor
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun NowTabClassDetails(
    course: CourseClass,
    isToday: Boolean,
    isEveningUnwind: Boolean,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val now = LocalTime.now()
    val isCompleted = isToday && course.endTime.isBefore(now)
    val isOngoing = isToday && !isCompleted && !course.startTime.isAfter(now)

    // Reusable color logic prioritizing isEveningUnwind over isCompleted
    val cardTextColor = when {
        isEveningUnwind && isCompleted -> SteamedMilk.copy(alpha = 0.75f)
        isEveningUnwind -> SteamedMilk
        isCompleted -> DarkMocha.copy(alpha = 0.6f)
        else -> DarkMocha
    }
    val cardIconTint = if (isEveningUnwind) SteamedMilk else DarkMocha.copy(alpha = 0.7f)
    val locationIconTint = if (isEveningUnwind) SteamedMilk else DeepEspresso

    val cardBg = when {
        isEveningUnwind -> Color(0xFF1E140F)
        isCompleted -> SteamedMilk.copy(alpha = 0.7f)
        else -> SteamedMilk
    }
    val borderColor = when {
        isOngoing -> GoldenCrema
        isEveningUnwind -> GoldenCrema.copy(alpha = 0.3f)
        isCompleted -> DarkMocha.copy(alpha = 0.15f)
        else -> DarkMocha.copy(alpha = 0.25f)
    }
    val borderWidth = if (isOngoing) 2.dp else 1.dp

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Class", fontWeight = FontWeight.Bold, color = if (isEveningUnwind) SteamedMilk else DarkMocha) },
            text = { Text("Are you sure you want to delete ${course.courseName} (${course.courseCode})?", color = if (isEveningUnwind) SteamedMilk.copy(alpha = 0.9f) else DarkMocha) },
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
                    Text("Cancel", color = if (isEveningUnwind) SteamedMilk else DarkMocha)
                }
            },
            containerColor = if (isEveningUnwind) Color(0xFF1E140F) else SteamedMilk
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                borderWidth,
                borderColor,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Subtle vertical accent strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(when {
                        isOngoing -> DeepEspresso
                        isCompleted -> WarmRoast.copy(alpha = 0.4f)
                        else -> GoldenCrema
                    })
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: Course Code + Course Name + More Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (isOngoing) DeepEspresso else WarmRoast,
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, GoldenCrema.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = SteamedMilk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
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
                                tint = cardIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(if (isEveningUnwind) Color(0xFF1E140F) else SteamedMilk)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Class", fontWeight = FontWeight.Bold, color = if (isEveningUnwind) SteamedMilk else DarkMocha) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = if (isEveningUnwind) SteamedMilk else DarkMocha, modifier = Modifier.size(16.dp))
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

                // Bottom Row: Time | Room Location | Status Badge
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time Information
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = cardIconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                                course.endTime.format(ScheduleConstants.TimeFormatter)
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardTextColor
                        )
                    }

                    // Room / Location Information
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Room Location",
                            tint = locationIconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = course.room,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = cardTextColor
                        )
                    }
                }
                // Status Badge
                if (isOngoing) {
                    Surface(
                        color = DeepEspresso,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "ONGOING",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SteamedMilk,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else if (isCompleted) {
                    Surface(
                        color = if (isEveningUnwind) SteamedMilk.copy(alpha = 0.15f) else DarkMocha.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "COMPLETED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEveningUnwind) SteamedMilk.copy(alpha = 0.85f) else DarkMocha.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}