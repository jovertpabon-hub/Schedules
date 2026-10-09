package com.example.schedules.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmRoast
import kotlinx.coroutines.launch

@Composable
fun CardsViewTab(
    weekSchedule: WeekSchedule,
    onEdit: (CourseClass, String) -> Unit = { _, _ -> },
    onDelete: (CourseClass) -> Unit = {},
    onVisibleDayChanged: (String) -> Unit = {},
    onBackToMain: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 0) { weekSchedule.days.size }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    BackHandler {
        onBackToMain()
    }

    // Report visible day for context-aware class additions
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page < weekSchedule.days.size) {
                onVisibleDayChanged(weekSchedule.days[page].dayName)
            }
        }
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
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBackToMain) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Weekly Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Screen 1: Cohesive 7-Day Strip Layout Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekSchedule.days.forEachIndexed { index, dayData ->
                val isSelected = (pagerState.currentPage == index)
                DayStripChip(
                    dayData = dayData,
                    isSelected = isSelected,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            }
        }

        // Screen 2: Day-by-Day Horizontal Pager with Timeline Breakdown
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            pageSpacing = 16.dp
        ) { page ->
            val dayData = weekSchedule.days[page]
            DetailedDayCard(
                dayData = dayData,
                onEdit = { onEdit(it, dayData.dayName) },
                onDelete = onDelete
            )
        }
      }
    }
}

@Composable
private fun DayStripChip(
    dayData: DayScheduleData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayAbbrev = when (dayData.dayName.lowercase()) {
        "monday" -> "MON"
        "tuesday" -> "TUE"
        "wednesday" -> "WED"
        "thursday" -> "THU"
        "friday" -> "FRI"
        "saturday" -> "SAT"
        "sunday" -> "SUN"
        else -> dayData.dayName.take(3).uppercase()
    }

    val classCountText = when (dayData.classes.size) {
        0 -> "Free"
        1 -> "1 class"
        else -> "${dayData.classes.size} classes"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dayAbbrev,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )

            // Standardized Material 3 Tonal Pill Tag
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = classCountText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Screen 1 & 2 - Weekly Strip Selector & Timeline Breakdown")
@Composable
fun CardsViewTabPreview() {
    val sampleWeek = WeekSchedule(
        days = listOf(
            DayScheduleData(
                dayName = "Monday",
                classes = listOf(
                    CourseClass(
                        id = 1,
                        courseCode = "CS 101",
                        courseName = "Data Structures & Algorithms",
                        startTime = java.time.LocalTime.of(9, 0),
                        endTime = java.time.LocalTime.of(10, 30),
                        room = "Lab 302",
                        instructor = "Dr. Alan Turing"
                    ),
                    CourseClass(
                        id = 2,
                        courseCode = "MATH 201",
                        courseName = "Linear Algebra & Calculus",
                        startTime = java.time.LocalTime.of(11, 0),
                        endTime = java.time.LocalTime.of(12, 30),
                        room = "Room 105",
                        instructor = "Prof. Euler"
                    ),
                    CourseClass(
                        id = 3,
                        courseCode = "PHYS 102",
                        courseName = "General Physics Lab",
                        startTime = java.time.LocalTime.of(14, 0),
                        endTime = java.time.LocalTime.of(16, 0),
                        room = "Science Hall 4",
                        instructor = "Unknown"
                    )
                )
            ),
            DayScheduleData(
                dayName = "Tuesday",
                classes = listOf(
                    CourseClass(
                        id = 4,
                        courseCode = "ENG 105",
                        courseName = "Technical Writing",
                        startTime = java.time.LocalTime.of(10, 0),
                        endTime = java.time.LocalTime.of(11, 30),
                        room = "Humanities 201",
                        instructor = "Dr. Maya Angelou"
                    )
                )
            ),
            DayScheduleData(dayName = "Wednesday", classes = emptyList()),
            DayScheduleData(dayName = "Thursday", classes = emptyList()),
            DayScheduleData(dayName = "Friday", classes = emptyList()),
            DayScheduleData(dayName = "Saturday", classes = emptyList()),
            DayScheduleData(dayName = "Sunday", classes = emptyList())
        )
    )

    com.example.schedules.ui.theme.SchedulesTheme {
        CardsViewTab(weekSchedule = sampleWeek)
    }
}
