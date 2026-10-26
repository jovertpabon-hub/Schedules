package com.example.schedules.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedules.data.models.ExtractedSchedule
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleImportPreview
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleImportPreviewDialog(
    preview: ScheduleImportPreview,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val totalCount = preview.totalEntries
    val buttonText = remember(totalCount) {
        if (totalCount == 1) "Import 1 Schedule" else "Import $totalCount Schedules"
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // 1. Header with Title, Subtitle, and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Review your schedules before saving.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Compact Import Summary
                ImportSummary(count = totalCount)

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Scrollable Schedule List with weight(1f, fill = false) for adaptive height
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    itemsIndexed(
                        items = preview.schedules,
                        key = { index, schedule ->
                            "${schedule.courseCode}_${schedule.dayOfWeek}_${schedule.startTime}_$index"
                        }
                    ) { index, schedule ->
                        CompactScheduleItem(schedule = schedule)
                        if (index < preview.schedules.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Primary Full-Width Action Button
                Button(
                    onClick = onConfirm,
                    enabled = totalCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportSummary(
    count: Int,
    modifier: Modifier = Modifier
) {
    val isSuccess = count > 0
    val summaryText = if (isSuccess) {
        if (count == 1) "✓ 1 schedule found" else "✓ $count schedules found"
    } else {
        "⚠ No valid schedules found in file"
    }

    val containerColor = if (isSuccess) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    }

    val contentColor = if (isSuccess) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CompactScheduleItem(
    schedule: ExtractedSchedule,
    modifier: Modifier = Modifier
) {
    val dayName = schedule.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(Locale.US)
    val startTimeFormatted = schedule.startTime.format(ScheduleConstants.TimeFormatter)
    val endTimeFormatted = schedule.endTime.format(ScheduleConstants.TimeFormatter)

    val timeAndLocationText = remember(startTimeFormatted, endTimeFormatted, schedule.room) {
        val hasRoom = schedule.room.isNotBlank() && !schedule.room.equals("TBA", ignoreCase = true)
        if (hasRoom) {
            "$startTimeFormatted–$endTimeFormatted · Room ${schedule.room}"
        } else {
            "$startTimeFormatted–$endTimeFormatted"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Abbreviated day label
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp)
        )

        // Subtle vertical separator
        Surface(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        // Schedule details column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = schedule.courseCode,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = schedule.courseName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeAndLocationText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
