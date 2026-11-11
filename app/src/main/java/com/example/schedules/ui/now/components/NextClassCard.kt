package com.example.schedules.ui.now.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmRoast
import java.time.LocalDate


@Composable
fun NowTabCountdownCard(
    status: ScheduleStatus,
    selectedDate: LocalDate = LocalDate.now(),
    onClick: () -> Unit = {}
) {
    val espressoGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF704515),  // Warm roasted brown
            0.35f to Color(0xFF4D2A00), // Your DeepEspresso
            0.70f to Color(0xFF3B2000), // Deep coffee brown
            1.0f to Color(0xFF291600)   // Dark espresso
        )
    )
    val infiniteTransition = rememberInfiniteTransition(label = "hintFade")

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    val today = remember { LocalDate.now() }
    val isToday = selectedDate == today

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.25.dp, GoldenCrema.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(espressoGradient)
        ) {
            CountdownTimerView(status = status)

            if (!isToday) {
                Text(
                    text = "Tap to return",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldenCrema.copy(alpha = textAlpha),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 10.dp)
                )
            }
        }
    }
}
@Composable
fun CountdownTimerView(
    status: ScheduleStatus
) {
    when (status.status) {
        ClassStatus.NONE -> {
            EmptyScheduleView(
                hasClassesToday = status.hasClassesToday
            )
        }

        ClassStatus.ONGOING,
        ClassStatus.UPCOMING -> {
            ActiveCountdownView(status = status)
        }
    }
}


@Composable
fun EmptyScheduleView(
    hasClassesToday: Boolean
) {
    val title = if (hasClassesToday) {
        "All Classes Finished"
    } else {
        "A Clear Day Ahead"
    }

    val description = if (hasClassesToday) {
        "You've completed your schedule for today."
    } else {
        "No classes scheduled today."
    }

    val supportingText = if (hasClassesToday) {
        "Enjoy your free time"
    } else {
        "Make the most of your day"
    }

    val icon = if (hasClassesToday) {
        Icons.Default.CheckCircle
    } else {
        Icons.Default.FreeBreakfast
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status header
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
                    imageVector = Icons.Default.FreeBreakfast,
                    contentDescription = null,
                    tint = GoldenCrema,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "DAILY OVERVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteamedMilk,
                    letterSpacing = 0.6.sp
                )
            }

            Surface(
                color = GoldenCrema,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.6f))
            ) {
                Text(
                    text = if (hasClassesToday) "COMPLETED" else "FREE DAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepEspresso,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
                )
            }
        }

        // Subtle horizontal divider anchor
        HorizontalDivider(
            color = SteamedMilk.copy(alpha = 0.15f),
            thickness = 1.dp
        )

        // Fixed timer and information area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = GoldenCrema.copy(alpha = 0.35f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "00:00:00",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = SteamedMilk,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk.copy(alpha = 0.75f),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SteamedMilk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SteamedMilk.copy(alpha = 0.75f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = GoldenCrema,
                            modifier = Modifier.size(13.dp)
                        )

                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldenCrema
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveCountdownView(
    status: ScheduleStatus
) {
    val course = status.courseClass ?: return
    val isOngoing = status.status == ClassStatus.ONGOING

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status header
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
                    imageVector = if (isOngoing)
                        Icons.Default.PlayCircleFilled
                    else
                        Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = GoldenCrema,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = if (isOngoing) "CURRENT CLASS" else "UP NEXT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteamedMilk,
                    letterSpacing = 0.6.sp
                )
            }

            Surface(
                color = GoldenCrema,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.6f))
            ) {
                Text(
                    text = course.courseCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepEspresso,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                )
            }
        }

        // Subtle horizontal divider anchor
        HorizontalDivider(
            color = SteamedMilk.copy(alpha = 0.15f),
            thickness = 1.dp
        )

        // Countdown and course information
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val strokeWidth = if (isOngoing) 6.dp.toPx() else 2.dp.toPx()

                    drawArc(
                        color = GoldenCrema.copy(
                            alpha = if (isOngoing) 0.35f else 0.25f
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    if (isOngoing) {
                        drawArc(
                            color = GoldenCrema,
                            startAngle = -90f,
                            sweepAngle = 360f * status.progressPercent.coerceIn(0f, 1f),
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
                        text = if (isOngoing) "REMAINING" else "STARTS IN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk.copy(alpha = 0.75f),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            VerticalDivider(color = SteamedMilk.copy(alpha = 0.15f), thickness = 1.dp)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SteamedMilk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(
                    color = SteamedMilk.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                // Grouped schedule details (time and room location)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                fontWeight = FontWeight.SemiBold,
                                color = SteamedMilk.copy(alpha = 0.95f),
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
