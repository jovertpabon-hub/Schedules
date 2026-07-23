package com.example.schedules

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

// --- Navigation Tabs & View States ---

enum class ScheduleTab(val title: String, val icon: ImageVector) {
    NOW("Now", Icons.Default.Schedule),
    GRID("Cards View", Icons.Default.GridView),
    TIMETABLE("Matrix View", Icons.Default.TableChart),
    TEMPLATE("Template View", Icons.Default.DateRange)
}

enum class CardDetailState {
    GRID_LIST,
    PAGER_DETAIL
}

enum class ClassStatus {
    ONGOING,
    UPCOMING,
    NONE
}

// --- Data Models ---

data class CourseClass(
    val courseCode: String,
    val courseName: String,
    val startTime: String,
    val endTime: String,
    val room: String = "TBA"
)

data class DayScheduleData(
    val dayName: String,
    val classes: List<CourseClass>
)

data class ScheduleStatus(
    val courseClass: CourseClass?,
    val dayName: String,
    val status: ClassStatus,
    val remainingFormatted: String,
    val progressPercent: Float = 0f
)

// --- Sample Dataset ---

val realWeekData = listOf(
    DayScheduleData(
        dayName = "Monday",
        classes = listOf(
            CourseClass("GE6", "Art Appreciation", "08:00 AM", "10:00 AM", "IITONLINE"),
            CourseClass("GE7", "Science, Tech & Society", "01:00 PM", "03:00 PM", "IITBLENDED"),
            CourseClass("CSP2", "Numerical Methods", "03:00 PM", "05:00 PM", "IITRM11"),
            CourseClass("NC1", "Networks & Communication", "05:00 PM", "06:00 PM", "IITRM6")
        )
    ),
    DayScheduleData(
        dayName = "Tuesday",
        classes = listOf(
            CourseClass("SDF1", "Object Oriented Programming", "08:00 AM", "09:00 AM", "IITRM7"),
            CourseClass("CC4", "Data Structures & Algorithm", "09:00 AM", "10:00 AM", "IITONLINE"),
            CourseClass("GE7", "Science, Tech & Society", "02:00 PM", "03:00 PM", "IITBLENDED"),
            CourseClass("PATHFIT3", "Dance", "03:00 PM", "05:00 PM", "GYM6")
        )
    ),
    DayScheduleData(
        dayName = "Wednesday",
        classes = listOf(
            CourseClass("GE6", "Art Appreciation", "08:00 AM", "09:00 AM", "IITONLINE"),
            CourseClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM12"),
            CourseClass("CC4", "Data Structures & Algorithm", "01:00 PM", "03:00 PM", "IITONLINE"),
            CourseClass("CC4", "Data Structures & Algorithm", "03:00 PM", "05:00 PM", "IITRM12")
        )
    ),
    DayScheduleData(
        dayName = "Thursday",
        classes = listOf(
            CourseClass("GEE3-C", "Living in the IT Era", "08:00 AM", "09:00 AM", "IITRM20"),
            CourseClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM7"),
            CourseClass("NC1", "Networks & Communication", "01:00 PM", "03:00 PM", "IITRM14"),
            CourseClass("CSP2", "Numerical Methods", "03:00 PM", "04:00 PM", "IITRM5")
        )
    ),
    DayScheduleData(
        dayName = "Friday",
        classes = listOf(
            CourseClass("NC1", "Networks & Communication", "08:00 AM", "09:00 AM", "IITRM6"),
            CourseClass("GEE3-C", "Living in the IT Era", "10:00 AM", "12:00 PM", "IITRM20"),
            CourseClass("CSP2", "Numerical Methods", "01:00 PM", "03:00 PM", "IITRM7")
        )
    ),
    DayScheduleData(dayName = "Saturday", classes = emptyList()),
    DayScheduleData(dayName = "Sunday", classes = emptyList())
)

// --- java.time Countdown Engine ---

@RequiresApi(Build.VERSION_CODES.O)
fun calculateCurrentStatus(weekData: List<DayScheduleData>): ScheduleStatus {
    val today = LocalDate.now().dayOfWeek.name
    val now = LocalTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    val todaySchedule = weekData.firstOrNull { it.dayName.equals(today, ignoreCase = true) }

    if (todaySchedule != null) {
        // 1. Check for ONGOING class
        for (course in todaySchedule.classes) {
            val startTime = runCatching { LocalTime.parse(course.startTime, timeFormatter) }.getOrNull()
            val endTime = runCatching { LocalTime.parse(course.endTime, timeFormatter) }.getOrNull()

            if (startTime != null && endTime != null && (now.isAfter(startTime) || now == startTime) && now.isBefore(endTime)) {
                val remainingSeconds = Duration.between(now, endTime).seconds
                val totalSeconds = Duration.between(startTime, endTime).seconds
                val elapsed = totalSeconds - remainingSeconds
                val progress = if (totalSeconds > 0) elapsed.toFloat() / totalSeconds.toFloat() else 0f

                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val seconds = remainingSeconds % 60

                return ScheduleStatus(
                    courseClass = course,
                    dayName = todaySchedule.dayName,
                    status = ClassStatus.ONGOING,
                    remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                    progressPercent = progress
                )
            }
        }

        // 2. Check for UPCOMING class TODAY
        val upcomingToday = todaySchedule.classes.firstOrNull { course ->
            val startTime = runCatching { LocalTime.parse(course.startTime, timeFormatter) }.getOrNull()
            startTime != null && now.isBefore(startTime)
        }

        if (upcomingToday != null) {
            val startTime = LocalTime.parse(upcomingToday.startTime, timeFormatter)
            val remainingSeconds = Duration.between(now, startTime).seconds

            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60

            return ScheduleStatus(
                courseClass = upcomingToday,
                dayName = todaySchedule.dayName,
                status = ClassStatus.UPCOMING,
                remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            )
        }
    }

    // 3. Look ahead to NEXT UPCOMING DAY with accurate multi-day countdown
    val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val todayIndex = dayOrder.indexOfFirst { it.equals(today, ignoreCase = true) }

    for (i in 1..6) {
        val nextDayIndex = if (todayIndex != -1) (todayIndex + i) % 7 else 0
        val nextDayName = dayOrder[nextDayIndex]
        val nextDaySchedule = weekData.firstOrNull { it.dayName == nextDayName }

        if (nextDaySchedule != null && nextDaySchedule.classes.isNotEmpty()) {
            val firstClass = nextDaySchedule.classes.first()
            val startTime = runCatching { LocalTime.parse(firstClass.startTime, timeFormatter) }.getOrNull()

            if (startTime != null) {
                // Total seconds remaining = remaining time today + days in between + target class start time
                val secondsTillEndOfToday = Duration.between(now, LocalTime.MAX).seconds + 1
                val secondsOnTargetDay = startTime.toSecondOfDay().toLong()
                val fullDaysInBetween = (i - 1).toLong()
                val totalRemainingSeconds = secondsTillEndOfToday + (fullDaysInBetween * 86400) + secondsOnTargetDay

                val hours = totalRemainingSeconds / 3600
                val minutes = (totalRemainingSeconds % 3600) / 60
                val seconds = totalRemainingSeconds % 60

                return ScheduleStatus(
                    courseClass = firstClass,
                    dayName = nextDaySchedule.dayName,
                    status = ClassStatus.UPCOMING,
                    remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                )
            }
        }
    }

    return ScheduleStatus(null, "Today", ClassStatus.NONE, "00:00:00")
}
// --- Main App Screen ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp() {
    var selectedTab by remember { mutableStateOf(ScheduleTab.NOW) }
    var currentStatus by remember { mutableStateOf(calculateCurrentStatus(realWeekData)) }

    // Ticks every 1 second
    LaunchedEffect(Unit) {
        while (true) {
            currentStatus = calculateCurrentStatus(realWeekData)
            delay(1000.milliseconds)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ScheduleTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IncomingScheduleBanner(status = currentStatus)

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    ScheduleTab.NOW -> NowTabScreen(status = currentStatus)
                    ScheduleTab.GRID -> CardsViewTab(weekData = realWeekData)
                    ScheduleTab.TIMETABLE -> TimetableMatrixTab(weekData = realWeekData)
                    ScheduleTab.TEMPLATE -> TemplateMatrixTab(weekData = realWeekData)
                }
            }
        }
    }
}

// --- Top Banner ---

@Composable
fun IncomingScheduleBanner(status: ScheduleStatus) {
    val course = status.courseClass

    val tagText = when (status.status) {
        ClassStatus.ONGOING -> "CLASS IN PROGRESS (${status.dayName.uppercase(Locale.US)})"
        ClassStatus.UPCOMING -> "NEXT CLASS (${status.dayName.uppercase(Locale.US)})"
        ClassStatus.NONE -> "SCHEDULE STATUS"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- 1. Top Bar: Tag Label + Live Countdown Pill ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tagText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                if (status.status != ClassStatus.NONE) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (status.status == ClassStatus.ONGOING)
                                "Ends: ${status.remainingFormatted}"
                            else
                                "Starts: ${status.remainingFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // --- Accent Separator Line ---
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- 2. Main Course Info ---
            if (course != null) {
                // Course Code and Name
                Text(
                    text = "${course.courseCode} - ${course.courseName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional Progress Bar for ongoing classes
                if (status.status == ClassStatus.ONGOING) {
                    LinearProgressIndicator(
                        progress = { status.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // --- 3. Bottom Info Row: Room and Time ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 Room: ${course.room}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "🕒 ${course.startTime} - ${course.endTime}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "No upcoming classes found 🎉",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- NOW Tab ---

@Composable
fun NowTabScreen(status: ScheduleStatus) {
    val course = status.courseClass

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Status Title
                Text(
                    text = when (status.status) {
                        ClassStatus.ONGOING -> "CLASS IN PROGRESS"
                        ClassStatus.UPCOMING -> "NEXT CLASS (${status.dayName.uppercase(Locale.US)})"
                        ClassStatus.NONE -> "NO CLASSES SCHEDULED"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                // Digital Countdown Display Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (status.status) {
                                ClassStatus.ONGOING -> "TIME REMAINING"
                                ClassStatus.UPCOMING -> "COUNTDOWN TO NEXT CLASS"
                                ClassStatus.NONE -> "COUNTDOWN TO NEXT SESSION"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = status.remainingFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (status.status == ClassStatus.ONGOING) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { status.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // --- RESTRUCTURED COURSE DETAILS CARD ---
                if (course != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Top Section: Code & Course Title
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = course.courseCode,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = course.courseName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // 2. Bottom Section: Info Badges Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Room Badge
                                InfoBadge(
                                    modifier = Modifier.weight(1f),
                                    label = "ROOM",
                                    value = course.room
                                )

                                // Time Schedule Badge
                                InfoBadge(
                                    modifier = Modifier.weight(1.5f),
                                    label = "TIME",
                                    value = "${course.startTime} - ${course.endTime}"
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No remaining classes scheduled. Rest up! 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// --- Helper Composable for Clean Badge Grid Layout ---

@Composable
private fun InfoBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        // Room Info
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Cards Tab ---

@Composable
fun CardsViewTab(weekData: List<DayScheduleData>) {
    var cardState by remember { mutableStateOf(CardDetailState.GRID_LIST) }
    val pagerState = rememberPagerState(initialPage = 0) { weekData.size }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = cardState == CardDetailState.PAGER_DETAIL) {
        cardState = CardDetailState.GRID_LIST
    }

    AnimatedContent(
        targetState = cardState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "CardViewTransition"
    ) { state ->
        when (state) {
            CardDetailState.GRID_LIST -> {
                val weekdays = weekData.take(6)
                val sunday = weekData.lastOrNull()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weekdays.size) { index ->
                        DayMiniCard(
                            dayData = weekdays[index],
                            onClick = {
                                coroutineScope.launch { pagerState.scrollToPage(index) }
                                cardState = CardDetailState.PAGER_DETAIL
                            }
                        )
                    }
                    if (sunday != null) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                DayMiniCard(
                                    dayData = sunday,
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                    onClick = {
                                        coroutineScope.launch { pagerState.scrollToPage(6) }
                                        cardState = CardDetailState.PAGER_DETAIL
                                    }
                                )
                            }
                        }
                    }
                }
            }
            CardDetailState.PAGER_DETAIL -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    DetailedDayCard(dayData = weekData[page])
                }
            }
        }
    }
}

@Composable
fun DayMiniCard(
    dayData: DayScheduleData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(150.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = dayData.dayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            if (dayData.classes.isEmpty()) {
                Text(
                    text = "No classes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                dayData.classes.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.courseCode,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.startTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                }
            }
        }
    }
}

@Composable
fun DetailedDayCard(dayData: DayScheduleData) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = dayData.dayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            if (dayData.classes.isEmpty()) {
                Text(
                    text = "No classes scheduled for this day",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(dayData.classes.size) { index ->
                        val item = dayData.classes[index]
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${item.courseCode} - ${item.courseName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Room: ${item.room}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${item.startTime} - ${item.endTime}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (index < dayData.classes.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Matrix Tab ---

@Composable
fun TimetableMatrixTab(weekData: List<DayScheduleData>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val slots = listOf("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM")

    val scheduleMap = remember(weekData) {
        weekData.flatMap { dayData ->
            dayData.classes.map { course ->
                "${dayData.dayName}_${course.startTime}" to course
            }
        }.toMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Text("Time", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .border(0.5.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        slots.forEach { slot ->
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slot.take(5), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                }
                fullDays.forEach { fullDay ->
                    val course = scheduleMap["${fullDay}_$slot"]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            .padding(1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (course != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = course.courseCode,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
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
}

// --- Template Tab ---

@Composable
fun TemplateMatrixTab(weekData: List<DayScheduleData>) {
    val periodsWithTime = listOf(
        "Morning" to "8:00 AM - 12:00PM",
        "Afternoon" to "12:00PM - 5:00PM",
        "Evening" to "5:00PM - 8:00PM"
    )

    var cardState by remember { mutableStateOf(CardDetailState.GRID_LIST) }
    val pagerState = rememberPagerState(initialPage = 0) { weekData.size }
    val coroutineScope = rememberCoroutineScope()

    // Handle System Back Button when zoomed into detail pager
    BackHandler(enabled = cardState == CardDetailState.PAGER_DETAIL) {
        cardState = CardDetailState.GRID_LIST
    }

    AnimatedContent(
        targetState = cardState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TemplateViewTransition"
    ) { state ->
        when (state) {
            CardDetailState.GRID_LIST -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    // --- Period Header Row ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        // Day Label Header Cell
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxSize()
                                .border(0.5.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Morning / Afternoon / Evening Header Cells with Time Ranges
                        periodsWithTime.forEach { (period, timeRange) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = period.uppercase(Locale.US),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = timeRange,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // --- Day Rows ---
                    weekData.forEachIndexed { index, dayData ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable {
                                    coroutineScope.launch { pagerState.scrollToPage(index) }
                                    cardState = CardDetailState.PAGER_DETAIL
                                }
                        ) {
                            // Day Column Header
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayData.dayName.take(3).uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Period Cells (Morning, Afternoon, Evening)
                            periodsWithTime.forEach { (period, _) ->
                                val matchingClasses = dayData.classes.filter { course ->
                                    when (period) {
                                        "Morning" -> course.startTime.contains("AM", ignoreCase = true)
                                        "Afternoon" -> course.startTime.contains("PM", ignoreCase = true) &&
                                                !course.startTime.startsWith("05") &&
                                                !course.startTime.startsWith("06")
                                        "Evening" -> course.startTime.contains("PM", ignoreCase = true) &&
                                                (course.startTime.startsWith("05") || course.startTime.startsWith("06"))
                                        else -> false
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        .padding(1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (matchingClasses.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            matchingClasses.forEach { item ->
                                                Text(
                                                    text = item.courseCode,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    textAlign = TextAlign.Center,
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
                }
            }

            // --- Detail Pager View when tapping a row ---
            CardDetailState.PAGER_DETAIL -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    DetailedDayCard(dayData = weekData[page])
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    NowTabScreen(calculateCurrentStatus(realWeekData))
}