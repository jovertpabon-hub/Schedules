package com.example.schedules.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
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
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NowTabHeader(
                status = status,
                onOpenCardsView = onOpenCardsView
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            NowTabCountdownCard(
                status = status,
                todayRemainingClasses = todayRemainingClasses
            )

            if (status.courseClass != null) {
                NowTabClassDetails(
                    course = status.courseClass,
                    onOpenCardsView = onOpenCardsView
                )
            } else {
                EmptyDayView(
                    modifier = Modifier.weight(1f),
                    mainMessage = "All clear for now!",
                    secondaryMessage = "You've finished your classes or none are scheduled today."
                )
            }
        }
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
        Text(
            text = when (status.status) {
                ClassStatus.ONGOING -> "CLASS IN PROGRESS"
                ClassStatus.UPCOMING -> "NEXT CLASS (${status.dayName.uppercase(Locale.US)})"
                ClassStatus.NONE -> "SCHEDULE COMPLETED"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )

        Surface(
            onClick = onOpenCardsView,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_coffee),
                    contentDescription = "Show Cards View",
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Cards",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
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

        if (classes.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                classes.forEach { classItem ->
                    ClassRowItem(classItem)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = "No more classes today!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        CardInstructionText(
            text = "Tap card to return to timer",
            icon = Icons.Default.Timer
        )
    }
}

@Composable
private fun ClassRowItem(classItem: CourseClass) {
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

            Spacer(modifier = Modifier.fillMaxWidth(0.04f))

            Text(
                text = "${classItem.startTime.format(ScheduleConstants.TimeFormatter)} - ${
                    classItem.endTime.format(ScheduleConstants.TimeFormatter)
                }",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CountdownTimerView(status: ScheduleStatus) {
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
                ClassStatus.NONE -> "FREEDOM!"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )

        Text(
            text = status.remainingFormatted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        if (status.status == ClassStatus.ONGOING) {
            OngoingProgressIndicator(status.progressPercent)
        }

        CardInstructionText(
            text = "Tap card to view upcoming schedule",
            icon = Icons.AutoMirrored.Filled.EventNote
        )
    }
}

@Composable
private fun OngoingProgressIndicator(progress: Float) {
    Spacer(modifier = Modifier.height(2.dp))
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(8.dp)
            .clip(CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.small
            ),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

@Composable
private fun CardInstructionText(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun NowTabClassDetails(
    course: CourseClass,
    onOpenCardsView: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ClassDetailsHeader(
                course = course,
                onOpenCardsView = onOpenCardsView
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ClassDetailsInfoGrid(course)
        }
    }
}

@Composable
private fun ClassDetailsHeader(
    course: CourseClass,
    onOpenCardsView: () -> Unit
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

        Surface(
            onClick = onOpenCardsView,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_coffee),
                contentDescription = "Show Cards View",
                modifier = Modifier
                    .padding(6.dp)
                    .size(36.dp)
            )
        }
    }
}

@Composable
private fun ClassDetailsInfoGrid(course: CourseClass) {
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
                label = "START TIME",
                value = course.startTime.format(ScheduleConstants.TimeFormatter),
                icon = Icons.Default.AccessTime
            )
            InfoBadge(
                modifier = Modifier.weight(1f),
                label = "END TIME",
                value = course.endTime.format(ScheduleConstants.TimeFormatter),
                icon = Icons.Default.AccessTime
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoBadge(
                modifier = Modifier.weight(1f),
                label = "ROOM",
                value = course.room,
                icon = Icons.Default.LocationOn
            )
            InfoBadge(
                modifier = Modifier.weight(1f),
                label = "INSTRUCTOR",
                value = course.instructor,
                icon = Icons.Default.Person
            )
        }
    }
}


