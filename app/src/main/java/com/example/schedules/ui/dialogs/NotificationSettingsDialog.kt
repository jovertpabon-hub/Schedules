package com.example.schedules.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.notifications.NotificationSettingsData
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // --- Dialog Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- Scrollable Settings Body ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ==========================================
                    // 1. CLASS REMINDERS
                    // ==========================================
                    SettingsCategoryCard(
                        title = "Class Reminders",
                        icon = Icons.Default.Schedule
                    ) {
                        SettingsSwitchRow(
                            title = "Enable Class Reminders",
                            subtitle = "Master toggle for all class notification alarms",
                            checked = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateGlobalNotifications(it) }
                        )

                        if (settings.globalEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text(
                                text = "Default Alert Time Before Class",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            val intervals = listOf(
                                0 to "At class start",
                                5 to "5 min before",
                                10 to "10 min before",
                                15 to "15 min before",
                                30 to "30 min before",
                                60 to "1 hour before"
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                intervals.forEach { (minutes, label) ->
                                    FilterChip(
                                        selected = settings.defaultOffsetMinutes == minutes,
                                        onClick = { viewModel.updateDefaultReminderOffset(minutes) },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Per-Class Override Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPerClassDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Per-Class Specific Reminders",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Set custom alert offsets for specific classes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Configure",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 2. NOTIFICATION TYPES
                    // ==========================================
                    SettingsCategoryCard(
                        title = "Notification Types",
                        icon = Icons.Default.Category
                    ) {
                        SettingsSwitchRow(
                            title = "Upcoming Classes",
                            subtitle = "Remind users before scheduled classes",
                            checked = settings.upcomingClassesEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateUpcomingClassesEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Class Starting",
                            subtitle = "Notify when a class is about to begin",
                            checked = settings.classStartingEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateClassStartingEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Schedule Changes",
                            subtitle = "Notify when class schedule is added, updated, or removed",
                            checked = settings.scheduleChangesEnabled,
                            enabled = settings.globalEnabled,
                            onCheckedChange = { viewModel.updateScheduleChangesEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Schedule Summary",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (settings.globalEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Receive a morning overview of your classes for the day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.dailySummaryEnabled,
                                onCheckedChange = { viewModel.updateDailySummaryEnabled(it) },
                                enabled = settings.globalEnabled,
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        if (settings.dailySummaryEnabled && settings.globalEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Summary Delivery Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(onClick = { showSummaryTimePicker = true }) {
                                    Text(
                                        text = settings.dailySummaryTime.format(ScheduleConstants.TimeFormatter),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3. QUIET HOURS
                    // ==========================================
                    SettingsCategoryCard(
                        title = "Quiet Hours",
                        icon = Icons.Default.Bedtime
                    ) {
                        SettingsSwitchRow(
                            title = "Enable Quiet Hours",
                            subtitle = "Silence optional alerts and deliver class reminders silently",
                            checked = settings.quietHoursEnabled,
                            onCheckedChange = { viewModel.updateQuietHoursEnabled(it) }
                        )

                        if (settings.quietHoursEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Start Time", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    TextButton(onClick = { showQuietStartPicker = true }) {
                                        Text(
                                            text = settings.quietHoursStart.format(ScheduleConstants.TimeFormatter),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("End Time", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    TextButton(onClick = { showQuietEndPicker = true }) {
                                        Text(
                                            text = settings.quietHoursEnd.format(ScheduleConstants.TimeFormatter),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "💡 Intended Behavior: Optional notifications (Summaries & Updates) are suppressed during Quiet Hours. Class reminders are delivered silently without sound or vibration.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 4. NOTIFICATION BEHAVIOR
                    // ==========================================
                    SettingsCategoryCard(
                        title = "Notification Behavior",
                        icon = Icons.Default.VolumeUp
                    ) {
                        SettingsSwitchRow(
                            title = "Sound",
                            subtitle = "Play sound when alert triggers",
                            checked = settings.soundEnabled,
                            onCheckedChange = { viewModel.updateSoundEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Vibration",
                            subtitle = "Vibrate phone on alert trigger",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { viewModel.updateVibrationEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Display Class Details",
                            subtitle = "Show course code, title, and room in notifications",
                            checked = settings.showClassDetails,
                            onCheckedChange = { viewModel.updateShowClassDetails(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Lock Screen Privacy",
                            subtitle = "Hide class details on lock screen notifications",
                            checked = settings.hideLockScreenDetails,
                            onCheckedChange = { viewModel.updateHideLockScreenDetails(it) }
                        )

                        Text(
                            text = "Note: On Android 8.0+, system notification channel settings controlled by OS will take precedence over app settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // ==========================================
                    // 5. SCHEDULE PREFERENCES
                    // ==========================================
                    SettingsCategoryCard(
                        title = "Schedule Preferences",
                        icon = Icons.Default.CalendarMonth
                    ) {
                        Text(
                            text = "Active School Days",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reminders will only trigger for classes on active school days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val allDays = ScheduleConstants.DaysOfWeek
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allDays.forEach { dayName ->
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SettingsSwitchRow(
                            title = "Weekend Reminders",
                            subtitle = "Enable class reminders on Saturdays and Sundays",
                            checked = settings.weekendRemindersEnabled,
                            onCheckedChange = { viewModel.updateWeekendRemindersEnabled(it) }
                        )
                    }
                }

                // --- Footer Action ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
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

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

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
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Per-Class Specific Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure custom alert intervals for individual classes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (enrolledClasses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No classes found in schedule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
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
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done", fontWeight = FontWeight.Bold)
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "$dayName @ ${course.startTime.format(ScheduleConstants.TimeFormatter)} (${course.room})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
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
