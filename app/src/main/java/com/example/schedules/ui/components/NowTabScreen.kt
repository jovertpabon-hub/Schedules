package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.R
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmCoffeeCream
import com.example.schedules.ui.theme.WarmRoast
import java.util.Locale

@Composable
fun NowTabScreen(
    status: ScheduleStatus,
    todayRemainingClasses: List<CourseClass> = emptyList(),
    onOpenCardsView: () -> Unit = {}
) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NowTabHeader(
                status = status,
                onOpenCardsView = onOpenCardsView
            )

            // Coffee Aroma Vapor Graphics
            CoffeeAromaVapor(modifier = Modifier.height(28.dp))

            NowTabCountdownCard(
                status = status,
                todayRemainingClasses = todayRemainingClasses
            )

            if (status.status == ClassStatus.NONE) {
                ScheduleCompletedStateView()
            } else if (status.courseClass != null) {
                NowTabClassDetails(
                    course = status.courseClass,
                    dayName = status.dayName,
                    onOpenCardsView = onOpenCardsView
                )
            } else {
                EmptyDayView(
                    modifier = Modifier.fillMaxWidth(),
                    mainMessage = "All clear for now!",
                    secondaryMessage = "You've finished your classes for today"
                )
            }
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
    val strokeWidth1 = remember { Stroke(width = 3.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round) }
    val strokeWidth2 = remember { Stroke(width = 4.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round) }

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
private fun NowTabHeader(
    status: ScheduleStatus,
    onOpenCardsView: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (status.status) {
                    ClassStatus.ONGOING -> "CLASS IN PROGRESS"
                    ClassStatus.UPCOMING -> "NEXT CLASS (${status.dayName.uppercase(Locale.US)})"
                    ClassStatus.NONE -> "SCHEDULE COMPLETED"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DarkMocha,
                letterSpacing = 0.8.sp
            )
        }

        Surface(
            onClick = onOpenCardsView,
            shape = RoundedCornerShape(50.dp),
            color = SteamedMilk,
            border = BorderStroke(1.dp, DarkMocha.copy(alpha = 0.4f)),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_coffee),
                    contentDescription = "Show Schedule Cards",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha
                )
            }
        }
    }
}

@Composable
private fun NowTabCountdownCard(
    status: ScheduleStatus,
    todayRemainingClasses: List<CourseClass>
) {
    var showUpcomingList by remember { mutableStateOf(false) }

    // Hero card container: Deep Espresso (#4D2A00) for hero card header/surface per UI/UX Designer spec
    val cardContainerColor = DeepEspresso

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                WarmRoast.copy(alpha = 0.5f),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { showUpcomingList = !showUpcomingList }
    ) {
        AnimatedContent(
            targetState = showUpcomingList,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                    animationSpec = tween(300)
                )
            },
            label = "CountdownScheduleFade"
        ) { isShowingList ->
            if (isShowingList) {
                RemainingClassesView(todayRemainingClasses)
            } else {
                CountdownTimerView(status)
            }
        }
    }
}

@Composable
private fun RemainingClassesView(classes: List<CourseClass>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TODAY'S REMAINING CLASSES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = SteamedMilk,
            letterSpacing = 0.8.sp
        )

        if (classes.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                classes.forEach { classItem ->
                    ClassRowItem(classItem)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = GoldenCrema
                )
                Text(
                    text = "No more classes today!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SteamedMilk,
                    textAlign = TextAlign.Center
                )
            }
        }

        CardInstructionText(
            text = "Tap card to return to timer",
            icon = Icons.Default.Timer,
            textColor = SteamedMilk.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ClassRowItem(classItem: CourseClass) {
    Surface(
        color = SteamedMilk,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Dark Mocha (#623528) for subject codes against Steamed Milk (#F9E6A8)
                Text(
                    text = classItem.courseCode,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkMocha
                )
                Text(
                    text = classItem.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkMocha,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = DeepEspresso,
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(0.5.dp, GoldenCrema.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "${classItem.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                        classItem.endTime.format(ScheduleConstants.TimeFormatter)
                    }",
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
private fun CountdownTimerView(status: ScheduleStatus) {
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
                    ClassStatus.NONE -> "SCHEDULE COMPLETED"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = SteamedMilk,
                letterSpacing = 0.8.sp
            )

            // Status badge in Golden Crema (#F2A900) or Warm Roast (#CC6F00)
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = when (status.status) {
                    ClassStatus.ONGOING -> GoldenCrema
                    ClassStatus.UPCOMING -> GoldenCrema
                    ClassStatus.NONE -> GoldenCrema
                },
                shadowElevation = 3.dp
            ) {
                Text(
                    text = when (status.status) {
                        ClassStatus.ONGOING -> "IN PROGRESS"
                        ClassStatus.UPCOMING -> "UPCOMING"
                        ClassStatus.NONE -> "FREEDOM!"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepEspresso,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Timer digits in Golden Crema (#F2A900) over Deep Espresso (#4D2A00) hero card
        Text(
            text = if (status.status == ClassStatus.NONE) "Uwian na! 🎉" else status.remainingFormatted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = GoldenCrema
        )

        // Progress Bar with Warm Roast (#CC6F00) filled progress bar
        if (status.status == ClassStatus.ONGOING) {
            OngoingProgressIndicator(status.progressPercent)
        }

        CardInstructionText(
            text = "Tap card to view upcoming schedule",
            icon = Icons.AutoMirrored.Filled.EventNote,
            textColor = SteamedMilk.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun OngoingProgressIndicator(progress: Float) {
    val percentInt = (progress * 100).toInt().coerceIn(0, 100)

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
                color = SteamedMilk.copy(alpha = 0.9f)
            )
            Surface(
                color = GoldenCrema,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "$percentInt%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepEspresso,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }

        // Warm Roast (#CC6F00) filled progress bar over Deep Espresso container
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
                .border(
                    1.dp,
                    GoldenCrema.copy(alpha = 0.4f),
                    CircleShape
                ),
            color = WarmRoast,
            trackColor = SteamedMilk.copy(alpha = 0.2f),
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
private fun ScheduleCompletedStateView() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DeepEspresso
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldenCrema.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(WarmRoast.copy(alpha = 0.25f), CircleShape)
                    .border(2.dp, GoldenCrema, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_coffee),
                    contentDescription = "Brewing Coffee Cup",
                    modifier = Modifier.size(56.dp)
                )
            }

            // FREEDOM! badge in Golden Crema
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = GoldenCrema,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, WarmRoast)
            ) {
                Text(
                    text = "☕ FREEDOM! ☕",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = DeepEspresso,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Text(
                text = "All classes completed for today. Time to enjoy a warm cup of coffee and unwind!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SteamedMilk,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NowTabClassDetails(
    course: CourseClass,
    dayName: String,
    onOpenCardsView: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SteamedMilk
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                DarkMocha.copy(alpha = 0.3f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkMocha,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = onOpenCardsView,
                    shape = CircleShape,
                    color = DeepEspresso,
                    border = BorderStroke(1.dp, GoldenCrema.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_coffee),
                        contentDescription = "Show Schedule Cards",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp)
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = DarkMocha.copy(alpha = 0.2f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    icon = Icons.Default.AccessTime,
                    label = "TIME SLOT",
                    value = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                        course.endTime.format(ScheduleConstants.TimeFormatter)
                    }"
                )

                ClassDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "SCHEDULED DAY",
                    value = dayName
                )
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
                // Dark Mocha (#623528) for readable labels against Steamed Milk / Warm Coffee Cream
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

// ==========================================
// Figma Presentation Showcase (16:9)
// ==========================================

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Figma Presentation - 5-Tone Coffee Theme (16:9)",
    widthDp = 1280,
    heightDp = 720,
    showBackground = true
)
@Composable
fun CoffeeAppPresentationScreen() {
    com.example.schedules.ui.theme.SchedulesTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepEspresso,
                            Color(0xFF3B1D02),
                            Color(0xFF261201),
                            Color(0xFF140A00)
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = GoldenCrema,
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "FIGMA DESIGN SYSTEM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = DeepEspresso,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "• 5-Tone Coffee Palette Integration",
                                style = MaterialTheme.typography.labelMedium,
                                color = SteamedMilk
                            )
                        }
                        Text(
                            text = "Professional Student Scheduling App UI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SteamedMilk
                        )
                    }

                    Surface(
                        color = WarmRoast,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldenCrema)
                    ) {
                        Text(
                            text = "Aspect Ratio 16:9",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GoldenCrema, CircleShape)
                            ) { }
                            Text(
                                text = "Screen 1: Class in Progress (Deep Espresso Hero & Warm Roast Progress)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SteamedMilk
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            border = BorderStroke(2.dp, GoldenCrema.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            NowTabScreen(
                                status = ScheduleStatus(
                                    status = ClassStatus.ONGOING,
                                    dayName = "Monday",
                                    remainingFormatted = "00:52:38",
                                    progressPercent = 0.65f,
                                    courseClass = CourseClass(
                                        id = 1,
                                        courseCode = "CS 101",
                                        courseName = "Data Structures & Algorithms",
                                        startTime = java.time.LocalTime.of(9, 0),
                                        endTime = java.time.LocalTime.of(10, 30),
                                        room = "Lab 302",
                                        instructor = "Dr. Alan Turing"
                                    )
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GoldenCrema, CircleShape)
                            ) { }
                            Text(
                                text = "Screen 2: Schedule Completed (FREEDOM! / Uwian na)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SteamedMilk
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            border = BorderStroke(2.dp, GoldenCrema.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            NowTabScreen(
                                status = ScheduleStatus(
                                    status = ClassStatus.NONE,
                                    dayName = "Monday",
                                    remainingFormatted = "Uwian na",
                                    progressPercent = 1.0f,
                                    courseClass = null
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5-Tone Palette: Dark Mocha (#623528) • Deep Espresso (#4D2A00) • Warm Roast (#CC6F00) • Golden Crema (#F2A900) • Steamed Milk (#F9E6A8)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteamedMilk.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Material 3 • Lead UI/UX Designer System",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldenCrema
                    )
                }
            }
        }
    }
}
