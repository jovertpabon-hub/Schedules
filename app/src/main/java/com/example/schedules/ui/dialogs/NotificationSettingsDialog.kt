package com.example.schedules.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import java.time.LocalTime

@Composable
fun NotificationSettingsDialog(
    viewModel: ScheduleViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.notificationSettings.collectAsState()
    val weekSchedule by viewModel.weekSchedule.collectAsState()

    var showPerClassDialog by remember { mutableStateOf(false) }
    var showSummaryTimePicker by remember { mutableStateOf(false) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // --- Dialog Header ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize class alerts and schedule notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // --- Scrollable Settings Body ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ==========================================
                    // SECTION A. GENERAL
                    // ==========================================
                    SettingsSection(title = "General") {
                        SettingsSwitchRow(
                            title = "Enable Class Reminders",
                            description = "Master control for all class reminder notifications",
                            checked = settings.globalEnabled,
                            onCheckedChange = viewModel::updateGlobalNotifications
                        )

                        AnimatedVisibility(
                            visible = settings.globalEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val intervals = listOf(
                                    0 to "At class start",
                                    5 to "5 min before",
                                    10 to "10 min before",
                                    15 to "15 min before",
                                    30 to "30 min before",
                                    60 to "1 hour before"
                                )
                                var intervalMenuExpanded by remember { mutableStateOf(false) }

                                Box {
                                    SettingsActionRow(
                                        title = "Default Reminder Interval",
                                        description = "How long before class start alerts should trigger",
                                        value = intervals.firstOrNull { it.first == settings.defaultOffsetMinutes }?.second ?: "15 min before",
                                        onClick = { intervalMenuExpanded = true }
                                    )

                                    DropdownMenu(
                                        expanded = intervalMenuExpanded,
                                        onDismissRequest = { intervalMenuExpanded = false }
                                    ) {
                                        intervals.forEach { (minutes, label) ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = label,
                                                        fontWeight = if (minutes == settings.defaultOffsetMinutes) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.updateDefaultReminderOffset(minutes)
                                                    intervalMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                SettingsActionRow(
                                    title = "Custom Per-Class Reminders",
                                    description = "Configure custom reminder offsets for specific classes",
                                    value = "Configure",
                                    onClick = { showPerClassDialog = true }
                                )
                            }
                        }
                    }

                    // ==========================================
                    // SECTION B. NOTIFICATION TYPES
                    // ==========================================
                    SettingsSection(title = "Notification Types") {
                        SettingsSwitchRow(
                            title = "Upcoming Classes",
                            description = "Receive advance reminder alerts for upcoming classes",
                            checked = settings.upcomingClassesEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateUpcomingClassesEnabled(it) }
                        )

                        SettingsSwitchRow(
                            title = "Class Starting",
                            description = "Receive an alert at the exact moment class begins",
                            checked = settings.classStartingEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateClassStartingEnabled(it) }
                        )

                        SettingsSwitchRow(
                            title = "Schedule Changes",
                            description = "Get notified when class rooms or schedule times change",
                            checked = settings.scheduleChangesEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateScheduleChangesEnabled(it) }
                        )

                        SettingsSwitchRow(
                            title = "Daily Schedule Summary",
                            description = "Receive a morning overview of your classes for the day",
                            checked = settings.dailySummaryEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateDailySummaryEnabled(it) }
                        )

                        AnimatedVisibility(
                            visible = settings.dailySummaryEnabled && settings.globalEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            SettingsTimeSelector(
                                label = "Summary Delivery Time",
                                time = settings.dailySummaryTime,
                                enabled = settings.globalEnabled,
                                onClick = { showSummaryTimePicker = true },
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // ==========================================
                    // SECTION C. QUIET HOURS
                    // ==========================================
                    SettingsSection(title = "Quiet Hours") {
                        SettingsSwitchRow(
                            title = "Enable Quiet Hours",
                            description = "Mute non-essential notifications during specified hours",
                            checked = settings.quietHoursEnabled,
                            onCheckedChange = { viewModel.updateQuietHoursEnabled(it) }
                        )

                        AnimatedVisibility(
                            visible = settings.quietHoursEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SettingsTimeSelector(
                                        label = "Start Time",
                                        time = settings.quietHoursStart,
                                        onClick = { showQuietStartPicker = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SettingsTimeSelector(
                                        label = "End Time",
                                        time = settings.quietHoursEnd,
                                        onClick = { showQuietEndPicker = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "💡 Optional notifications (Summaries & Updates) are suppressed during Quiet Hours. Class reminders are delivered silently.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION D. PREFERENCES
                    // ==========================================
                    SettingsSection(title = "Preferences", showDivider = false) {
                        SettingsSwitchRow(
                            title = "Sound",
                            description = "Play alert sound when notifications arrive",
                            checked = settings.soundEnabled,
                            onCheckedChange = { viewModel.updateSoundEnabled(it) }
                        )

                        SettingsSwitchRow(
                            title = "Vibration",
                            description = "Vibrate phone when notifications arrive",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { viewModel.updateVibrationEnabled(it) }
                        )

                        SettingsSwitchRow(
                            title = "Display Class Details",
                            description = "Show course name, room, and code in notification body",
                            checked = settings.showClassDetails,
                            onCheckedChange = { viewModel.updateShowClassDetails(it) }
                        )

                        SettingsSwitchRow(
                            title = "Lock Screen Privacy",
                            description = "Hide class details on the lock screen for privacy",
                            checked = settings.hideLockScreenDetails,
                            onCheckedChange = { viewModel.updateHideLockScreenDetails(it) }
                        )

                        Text(
                            text = "Note: On Android 8.0+, system notification channel settings take precedence over app settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        ExpandableSettingsSection(
                            title = "Schedule Preferences",
                            description = "Configure active school days and weekend reminder behavior",
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Active School Days",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Reminders trigger only for classes on active school days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ScheduleConstants.DaysOfWeek.forEach { dayName ->
                                    val isSelected = settings.schoolDays.contains(dayName)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleSchoolDay(dayName) },
                                        label = { Text(dayName.take(3)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            SettingsSwitchRow(
                                title = "Weekend Reminders",
                                description = "Send notifications on weekends if classes are scheduled",
                                checked = settings.weekendRemindersEnabled,
                                onCheckedChange = { viewModel.updateWeekendRemindersEnabled(it) }
                            )
                        }
                    }
                }

                // --- Footer Action ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // --- Time Pickers ---
    if (showSummaryTimePicker) {
        TimePickerDialog(
            initialTime = settings.dailySummaryTime,
            onDismiss = { showSummaryTimePicker = false },
            onConfirm = { time ->
                viewModel.updateDailySummaryTime(time.hour, time.minute)
                showSummaryTimePicker = false
            }
        )
    }

    if (showQuietStartPicker) {
        TimePickerDialog(
            initialTime = settings.quietHoursStart,
            onDismiss = { showQuietStartPicker = false },
            onConfirm = { time ->
                viewModel.updateQuietHoursStart(time.hour, time.minute)
                showQuietStartPicker = false
            }
        )
    }

    if (showQuietEndPicker) {
        TimePickerDialog(
            initialTime = settings.quietHoursEnd,
            onDismiss = { showQuietEndPicker = false },
            onConfirm = { time ->
                viewModel.updateQuietHoursEnd(time.hour, time.minute)
                showQuietEndPicker = false
            }
        )
    }

    // --- Per Class Reminders Override Sub-Dialog ---
    if (showPerClassDialog) {
        val allEnrolledClasses = remember(weekSchedule) {
            weekSchedule.days.flatMap { dayData ->
                dayData.classes.map { course -> dayData.dayName to course }
            }
        }

        PerClassRemindersDialog(
            enrolledClasses = allEnrolledClasses,
            perClassOffsets = settings.perClassOffsets,
            globalDefaultOffset = settings.defaultOffsetMinutes,
            onDismiss = { showPerClassDialog = false },
            onUpdateOffset = { classId, offset ->
                viewModel.updatePerClassReminderOffset(classId, offset)
            }
        )
    }
}

// ============================================================================
// REUSABLE SETTINGS COMPONENTS
// ============================================================================

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        content()
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SettingsTimeSelector(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = time.format(ScheduleConstants.TimeFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ExpandableSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    initialExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

// ============================================================================
// PER-CLASS REMINDERS DIALOG
// ============================================================================

@Composable
fun PerClassRemindersDialog(
    enrolledClasses: List<Pair<String, CourseClass>>,
    perClassOffsets: Map<Long, Int>,
    globalDefaultOffset: Int,
    onDismiss: () -> Unit,
    onUpdateOffset: (Long, Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Per-Class Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure custom alert intervals for individual classes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                if (enrolledClasses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No classes found in your schedule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        enrolledClasses.forEach { (dayName, course) ->
                            course.id?.let { classId ->
                                val currentCustomOffset = perClassOffsets[classId] ?: -1
                                CourseClassReminderRow(
                                    dayName = dayName,
                                    course = course,
                                    currentCustomOffset = currentCustomOffset,
                                    globalDefaultOffset = globalDefaultOffset,
                                    onSelectOffset = { newOffset ->
                                        onUpdateOffset(classId, newOffset)
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseClassReminderRow(
    dayName: String,
    course: CourseClass,
    currentCustomOffset: Int,
    globalDefaultOffset: Int,
    onSelectOffset: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val options = listOf(
        -1 to "Global Default ($globalDefaultOffset min)",
        0 to "At class start time",
        5 to "5 min before",
        10 to "10 min before",
        15 to "15 min before",
        30 to "30 min before",
        60 to "1 hour before"
    )

    val currentLabel = options.firstOrNull { it.first == currentCustomOffset }?.second
        ?: "Global Default ($globalDefaultOffset min)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.courseCode} – ${course.courseName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$dayName @ ${course.startTime.format(ScheduleConstants.TimeFormatter)} (${course.room})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = currentLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { (offset, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        fontWeight = if (offset == currentCustomOffset) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectOffset(offset)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
