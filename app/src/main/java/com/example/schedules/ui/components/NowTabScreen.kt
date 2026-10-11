package com.example.schedules.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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
import kotlin.time.Duration.Companion.milliseconds

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFEBEBF4),
                Color(0xFFF4F4F9),
                Color(0xFFEBEBF4)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun SkeletonClassCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SteamedMilk),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .border(1.5.dp, DarkMocha.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )
        }
    }
}

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

    val isTodaySelected = selectedDay.equals(todayName, ignoreCase = true)
    val selectedDayData = remember(weekSchedule, selectedDay) {
        weekSchedule.days.firstOrNull { it.dayName.equals(selectedDay, ignoreCase = true) }
            ?: DayScheduleData(selectedDay, emptyList())
    }

    var isDetailsExpanded by remember { mutableStateOf(true) }
    var showVacantTimes by remember { mutableStateOf(false) }
    var isLoadingDayData by remember { mutableStateOf(false) }

    val vacantBlocks = remember(selectedDayData.classes) {
        calculateVacantTimeBlocks(selectedDayData.classes)
    }

    LaunchedEffect(selectedDay) {
        isLoadingDayData = true
        kotlinx.coroutines.delay(180.milliseconds)
        isLoadingDayData = false
        isDetailsExpanded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFBF2),
                        SteamedMilk.copy(alpha = 0.75f),
                        GoldenCrema.copy(alpha = 0.35f),
                        WarmRoast.copy(alpha = 0.12f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NowTabHeader(
                selectedDay = selectedDay,
                weekSchedule = weekSchedule,
                onSelectedDayChanged = onSelectedDayChanged
            )

            // Coffee Aroma Vapor Graphics
            CoffeeAromaVapor(modifier = Modifier.height(24.dp))

            // MAIN OVERVIEW BOX (DEFAULT STARTING VIEW WITH DYNAMIC COLOR TRANSITION)
            if (isTodaySelected) {
                NowTabCountdownCard(
                    status = status,
                    isExpanded = isDetailsExpanded,
                    onToggleExpand = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isDetailsExpanded = !isDetailsExpanded
                    }
                )
            } else {
                DaySummaryHeroCard(
                    dayData = selectedDayData,
                    isExpanded = isDetailsExpanded,
                    onToggleExpand = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isDetailsExpanded = !isDetailsExpanded
                    }
                )
            }

            // DETAILED SCHEDULE LIST OR VACANT TIMES BREAKDOWN WITH SPRING EXPANSION
            AnimatedVisibility(
                visible = isDetailsExpanded,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Row with Title and Swap/Filter Toggle Icon Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showVacantTimes) {
                                "${selectedDay.uppercase(Locale.US)} VACANT TIMES (${vacantBlocks.size})"
                            } else {
                                "${selectedDay.uppercase(Locale.US)} DETAILED SCHEDULE (${selectedDayData.classes.size})"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkMocha,
                            modifier = Modifier.weight(1f),
                            letterSpacing = 0.8.sp
                        )

                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showVacantTimes = !showVacantTimes
                            },
                            shape = RoundedCornerShape(50.dp),
                            color = if (showVacantTimes) GoldenCrema else SteamedMilk,
                            border = BorderStroke(1.dp, DarkMocha.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap between busy and vacant times",
                                    tint = DeepEspresso,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (isLoadingDayData) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(3) {
                                SkeletonClassCard()
                            }
                        }
                    } else if (showVacantTimes) {
                        // VACANT TIME BLOCKS VIEW
                        if (vacantBlocks.isEmpty()) {
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
                                vacantBlocks.forEach { block ->
                                    VacantTimeBlockCard(block = block)
                                }
                            }
                        }
                    } else {
                        // BUSY CLASSES VIEW
                        if (selectedDayData.classes.isEmpty()) {
                            EmptyDayView(
                                modifier = Modifier.fillMaxWidth(),
                                mainMessage = "No classes for $selectedDay!",
                                secondaryMessage = "Enjoy a free day or catch up on studies"
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedDayData.classes.forEach { course ->
                                    NowTabClassDetails(
                                        course = course,
                                        dayName = selectedDayData.dayName,
                                        onEdit = { onEditClass(course, selectedDayData.dayName) },
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                GoldenCrema,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GoldenCrema,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "VACANT TIME BLOCK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepEspresso,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Vacant Slot Time",
                    tint = DarkMocha,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${block.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                        block.endTime.format(ScheduleConstants.TimeFormatter)
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkMocha
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = DarkMocha.copy(alpha = 0.2f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FreeBreakfast,
                    contentDescription = null,
                    tint = WarmRoast,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Free gap window • Great time for study or a coffee break ☕",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = DarkMocha.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun NowTabHeader(
    selectedDay: String,
    weekSchedule: WeekSchedule,
    onSelectedDayChanged: (String) -> Unit
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = DarkMocha
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isToday) "TODAY'S SCHEDULE" else "${selectedDay.uppercase(Locale.US)} SCHEDULE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DarkMocha.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
        }

        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(50.dp),
                color = SteamedMilk,
                border = BorderStroke(1.dp, DarkMocha.copy(alpha = 0.4f)),
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
                        color = DarkMocha
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Menu",
                        tint = DarkMocha,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SteamedMilk)
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
                                    color = DarkMocha
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
    dayData: DayScheduleData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val cardContainerColor by animateColorAsState(
        targetValue = if (isExpanded) SteamedMilk else DeepEspresso,
        animationSpec = tween(300),
        label = "DayHeroContainer"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) DarkMocha.copy(alpha = 0.3f) else GoldenCrema.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "DayHeroBorder"
    )

    val textColorPrimary by animateColorAsState(
        targetValue = if (isExpanded) DarkMocha else SteamedMilk,
        animationSpec = tween(300),
        label = "DayHeroTextPrimary"
    )

    val textColorAccent by animateColorAsState(
        targetValue = if (isExpanded) DeepEspresso else GoldenCrema,
        animationSpec = tween(300),
        label = "DayHeroTextAccent"
    )

    val badgeBgColor by animateColorAsState(
        targetValue = if (isExpanded) DeepEspresso else GoldenCrema,
        animationSpec = tween(300),
        label = "DayHeroBadgeBg"
    )

    val badgeTextColor by animateColorAsState(
        targetValue = if (isExpanded) SteamedMilk else DeepEspresso,
        animationSpec = tween(300),
        label = "DayHeroBadgeText"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                borderColor,
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onToggleExpand() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dayData.dayName.uppercase(Locale.US)} OVERVIEW",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColorPrimary,
                    letterSpacing = 0.8.sp
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = badgeBgColor,
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = when (dayData.classes.size) {
                            0 -> "FREE DAY"
                            1 -> "1 CLASS"
                            else -> "${dayData.classes.size} CLASSES"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            if (dayData.classes.isNotEmpty()) {
                val firstStart = dayData.classes.first().startTime.format(ScheduleConstants.TimeFormatter)
                val lastEnd = dayData.classes.last().endTime.format(ScheduleConstants.TimeFormatter)

                Text(
                    text = "$firstStart - $lastEnd",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = textColorAccent
                )
            } else {
                Text(
                    text = "No Classes Scheduled ☕",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColorAccent
                )
            }

            CardInstructionText(
                text = if (isExpanded) "Tap box to hide detailed schedule" else "Tap box to view detailed schedule",
                icon = Icons.AutoMirrored.Filled.EventNote,
                textColor = textColorPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CoffeeAromaVapor(modifier: Modifier = Modifier) {
    val vaporColor = GoldenCrema.copy(alpha = 0.75f)
    val vaporColorHighlight = remember(vaporColor) { vaporColor.copy(alpha = 0.9f) }
    val path1 = remember { Path() }
    val path2 = remember { Path() }
    val path3 = remember { Path() }
    val strokeWidth1 = remember { Stroke(width = 3.5f, cap = StrokeCap.Round) }
    val strokeWidth2 = remember { Stroke(width = 4.5f, cap = StrokeCap.Round) }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        path1.reset()
        path1.moveTo(width * 0.35f, height)
        path1.cubicTo(width * 0.3f, height * 0.7f, width * 0.4f, height * 0.4f, width * 0.33f, 0f)

        path2.reset()
        path2.moveTo(width * 0.5f, height)
        path2.cubicTo(width * 0.55f, height * 0.65f, width * 0.45f, height * 0.35f, width * 0.52f, 0f)

        path3.reset()
        path3.moveTo(width * 0.65f, height)
        path3.cubicTo(width * 0.7f, height * 0.7f, width * 0.6f, height * 0.4f, width * 0.67f, 0f)

        drawPath(path1, color = vaporColor, style = strokeWidth1)
        drawPath(path2, color = vaporColorHighlight, style = strokeWidth2)
        drawPath(path3, color = vaporColor, style = strokeWidth1)
    }
}

@Composable
private fun NowTabCountdownCard(
    status: ScheduleStatus,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val cardContainerColor by animateColorAsState(
        targetValue = if (isExpanded) SteamedMilk else DeepEspresso,
        animationSpec = tween(300),
        label = "CountdownCardColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) DarkMocha.copy(alpha = 0.3f) else GoldenCrema.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "CountdownBorderColor"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                borderColor,
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onToggleExpand() }
    ) {
        CountdownTimerView(status = status, isExpanded = isExpanded)
    }
}

@Composable
private fun CountdownTimerView(
    status: ScheduleStatus,
    isExpanded: Boolean
) {
    val textColorPrimary by animateColorAsState(
        targetValue = if (isExpanded) DarkMocha else SteamedMilk,
        animationSpec = tween(300),
        label = "TimerTextPrimary"
    )

    val textColorAccent by animateColorAsState(
        targetValue = if (isExpanded) DeepEspresso else GoldenCrema,
        animationSpec = tween(300),
        label = "TimerTextAccent"
    )

    val badgeBgColor by animateColorAsState(
        targetValue = if (isExpanded) DeepEspresso else GoldenCrema,
        animationSpec = tween(300),
        label = "TimerBadgeBg"
    )

    val badgeTextColor by animateColorAsState(
        targetValue = if (isExpanded) SteamedMilk else DeepEspresso,
        animationSpec = tween(300),
        label = "TimerBadgeText"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "LiveBeaconTransition")
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeaconPulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 22.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (status.status) {
                    ClassStatus.ONGOING -> "TIME REMAINING"
                    ClassStatus.UPCOMING -> "NEXT CLASS"
                    ClassStatus.NONE -> "TODAY'S OVERVIEW"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColorPrimary,
                letterSpacing = 0.8.sp
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = badgeBgColor,
                shadowElevation = 3.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (status.status == ClassStatus.ONGOING) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isExpanded) GoldenCrema.copy(alpha = beaconPulseAlpha) else WarmRoast.copy(alpha = beaconPulseAlpha))
                        )
                    }
                    Text(
                        text = when (status.status) {
                            ClassStatus.ONGOING -> "IN PROGRESS"
                            ClassStatus.UPCOMING -> "UPCOMING"
                            ClassStatus.NONE -> "SCHEDULE DONE"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor
                    )
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            if (status.status == ClassStatus.ONGOING) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    drawArc(
                        color = textColorPrimary.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = textColorAccent,
                        startAngle = 135f,
                        sweepAngle = 270f * status.progressPercent.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Text(
                text = if (status.status == ClassStatus.NONE) "All Done! 🎉" else status.remainingFormatted,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = textColorAccent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (status.status == ClassStatus.ONGOING) {
            OngoingProgressIndicator(
                progress = status.progressPercent,
                isExpanded = isExpanded
            )
        }

        CardInstructionText(
            text = if (isExpanded) "Tap box to hide detailed schedule" else "Tap box to view detailed schedule",
            icon = Icons.AutoMirrored.Filled.EventNote,
            textColor = textColorPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun OngoingProgressIndicator(
    progress: Float,
    isExpanded: Boolean = false
) {
    val percentInt = (progress * 100).toInt().coerceIn(0, 100)

    val textColor by animateColorAsState(
        targetValue = if (isExpanded) DarkMocha else SteamedMilk,
        animationSpec = tween(300),
        label = "ProgressTextColor"
    )

    val badgeBg by animateColorAsState(
        targetValue = if (isExpanded) DeepEspresso else GoldenCrema,
        animationSpec = tween(300),
        label = "ProgressBadgeBg"
    )

    val badgeText by animateColorAsState(
        targetValue = if (isExpanded) SteamedMilk else DeepEspresso,
        animationSpec = tween(300),
        label = "ProgressBadgeText"
    )

    Column(
        modifier = Modifier.fillMaxWidth(0.92f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Class Progress",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.9f)
            )
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "$percentInt%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
                .border(
                    1.dp,
                    if (isExpanded) DarkMocha.copy(alpha = 0.3f) else GoldenCrema.copy(alpha = 0.4f),
                    CircleShape
                ),
            color = if (isExpanded) DeepEspresso else WarmRoast,
            trackColor = textColor.copy(alpha = 0.2f),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
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
            modifier = Modifier.size(14.dp),
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
    dayName: String,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SteamedMilk
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                DarkMocha.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isExpanded = !isExpanded
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        color = DeepEspresso,
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, GoldenCrema.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = course.courseCode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SteamedMilk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkMocha,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Class",
                            tint = DarkMocha,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Class",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand Dropdown",
                        tint = DarkMocha,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

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
                        tint = DarkMocha.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                            course.endTime.format(ScheduleConstants.TimeFormatter)
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkMocha
                    )
                }

                Text(
                    text = if (isExpanded) "Tap to collapse" else "Tap to view details ▾",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkMocha.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(250)) + expandVertically(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(animationSpec = tween(250))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = DarkMocha.copy(alpha = 0.2f)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "ROOM",
                            value = course.room
                        )

                        ClassDetailRow(
                            icon = Icons.Default.Person,
                            label = "INSTRUCTOR",
                            value = course.instructor
                        )

                        ClassDetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "SCHEDULED DAY",
                            value = dayName
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current

                        AssistChip(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(course.room))
                                Toast.makeText(context, "Room ${course.room} copied", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("Copy Room", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Room",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = WarmCoffeeCream,
                                labelColor = DarkMocha,
                                leadingIconContentColor = DarkMocha
                            ),
                            border = BorderStroke(0.5.dp, DarkMocha.copy(alpha = 0.3f))
                        )

                        AssistChip(
                            onClick = {
                                Toast.makeText(context, "Reminder active for class", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("Reminder", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Reminder",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = WarmCoffeeCream,
                                labelColor = DarkMocha,
                                leadingIconContentColor = DarkMocha
                            ),
                            border = BorderStroke(0.5.dp, DarkMocha.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        color = WarmCoffeeCream,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, DarkMocha.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = DeepEspresso,
                shape = CircleShape,
                border = BorderStroke(1.dp, GoldenCrema.copy(alpha = 0.4f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = SteamedMilk,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
