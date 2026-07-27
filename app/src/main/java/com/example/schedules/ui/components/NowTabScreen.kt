package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.R
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleStatus
import java.util.Locale

@Composable
fun NowTabScreen(
    status: ScheduleStatus,
    todayRemainingClasses: List<CourseClass> = emptyList()
) {
    val course = status.courseClass
    var showUpcomingList by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                        .clickable { showUpcomingList = !showUpcomingList }
                ) {
                    AnimatedContent(
                        targetState = showUpcomingList,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "CountdownScheduleFade"
                    ) { isShowingList ->
                        if (isShowingList) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "TODAY'S REMAINING CLASSES",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )

                                if (todayRemainingClasses.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        todayRemainingClasses.forEach { classItem ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                shape = MaterialTheme.shapes.extraSmall
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${classItem.courseCode} - ${classItem.courseName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "${classItem.startTime} - ${classItem.endTime}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No remaining classes scheduled for today.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Text(
                                    text = "🍯 Tap card to return to timer",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { status.progressPercent },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                                        gapSize = 0.dp,
                                        drawStopIndicator = {}
                                    )
                                }

                                Text(
                                    text ="🐝 Tap card to view upcoming schedule",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                if (course != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
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
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
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

                                Image(
                                    painter = painterResource(R.drawable.honey_pot),
                                    contentDescription = "Honey Pot Icon",
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(42.dp)
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    InfoBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "🕒 START TIME",
                                        value = course.startTime
                                    )
                                    InfoBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "🕒 END TIME",
                                        value = course.endTime
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    InfoBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "📍 ROOM",
                                        value = course.room
                                    )
                                    InfoBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "👤 INSTRUCTOR",
                                        value = course.instructor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No remaining classes scheduled. Rest up!",
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

