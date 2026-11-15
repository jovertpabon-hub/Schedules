package com.example.schedules.ui.now.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.ui.dialogs.ClassNoteDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NowTabClassDetails(
    course: CourseClass,
    isToday: Boolean,
    currentTime: LocalTime,
    selectedDate: LocalDate,
    note: String?,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onSaveNote: (String) -> Unit = {},
    onDeleteNote: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val isCompleted = isToday && !currentTime.isBefore(course.endTime)
    val isOngoing = isToday && !currentTime.isBefore(course.startTime) && currentTime.isBefore(course.endTime)

    val cardTextColor = when {
        isCompleted -> colors.onSurface.copy(alpha = 0.55f)
        else -> colors.onSurface
    }
    val iconTint = colors.onSurfaceVariant.copy(alpha = 0.75f)

    val cardBg = when {
        isCompleted -> colors.surfaceVariant.copy(alpha = 0.7f)
        else -> colors.surfaceVariant
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Class", fontWeight = FontWeight.Bold, color = colors.onSurface) },
            text = { Text("Are you sure you want to delete ${course.courseName} (${course.courseCode})?", color = colors.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = colors.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = colors.onSurface)
                }
            },
            containerColor = colors.surface
        )
    }

    if (showNoteDialog) {
        ClassNoteDialog(
            course = course,
            selectedDate = selectedDate,
            existingNote = note,
            onDismiss = { showNoteDialog = false },
            onSave = onSaveNote,
            onDelete = onDeleteNote
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 1.dp else 0.dp),
        border = BorderStroke(
            width = 1.25.dp,
            color = when {
                isOngoing -> colors.secondary
                isCompleted -> colors.outlineVariant.copy(alpha = 0.35f)
                else -> colors.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = course.courseCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )

                        if (isToday && isOngoing) {
                            Surface(
                                color = colors.primaryContainer,
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "ONGOING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isToday && isCompleted) {
                            Surface(
                                color = colors.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "DONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
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
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Class", fontWeight = FontWeight.Bold, color = colors.onSurface) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note.isNullOrBlank()) "Add Temporary Note" else "Edit Temporary Note", fontWeight = FontWeight.Bold, color = colors.onSurface) },
                            onClick = {
                                showMenu = false
                                showNoteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Event, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Class", fontWeight = FontWeight.Bold, color = colors.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            if (!note.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = colors.outlineVariant.copy(alpha = 0.4f)
                )
                Surface(
                    color = colors.secondary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Temporary Note",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temporary Note (${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = cardTextColor
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = colors.outlineVariant.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Class time",
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${course.startTime.format(ScheduleConstants.TimeFormatter)} - ${course.endTime.format(ScheduleConstants.TimeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = cardTextColor
                    )
                }

                if (course.room.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Room location",
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = course.room,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = cardTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
