package com.example.schedules.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.ui.theme.SchedulesTheme
import com.example.schedules.ui.viewmodel.ScheduleViewModel

@Composable
fun SettingsOverviewDialog(
    onDismiss: () -> Unit,
    onNotifications: () -> Unit,
    onSchedule: () -> Unit,
    onAppearance: () -> Unit,
    onAbout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.94f).fillMaxHeight(.82f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Manage your CoffeeLog experience", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close settings")
                    }
                }
                HorizontalDivider(Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                    SettingsNavigationRow(Icons.Default.Notifications, "Notifications", "Reminders, quiet hours and notification behavior", onNotifications)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavigationRow(Icons.Default.CalendarMonth, "Schedule", "School days and timetable preferences", onSchedule)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavigationRow(Icons.Default.Palette, "Appearance", "Theme and visual preferences", onAppearance)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavigationRow(Icons.Default.Info, "About", "App version and information", onAbout)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Done") }
                }
            }
        }
    }
}

@Composable
fun SettingsNavigationRow(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 104.dp).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f).padding(start = 18.dp, end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open $title settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ScheduleSettingsDialog(viewModel: ScheduleViewModel, onDismiss: () -> Unit) {
    val settings by viewModel.notificationSettings.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(.8f), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Schedule", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close schedule settings") }
                }
                Text("Active school days", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    ScheduleConstants.DaysOfWeek.forEach { day ->
                        Row(Modifier.fillMaxWidth().clickable { viewModel.toggleSchoolDay(day) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(day, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Checkbox(checked = day in settings.schoolDays, onCheckedChange = { viewModel.toggleSchoolDay(day) })
                        }
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Weekend reminders", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(settings.weekendRemindersEnabled, viewModel::updateWeekendRemindersEnabled)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("Done") } }
            }
        }
    }
}

@Composable
fun AppearanceSettingsDialog(themeMode: String, onThemeModeChange: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(22.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Choose how CoffeeLog looks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onThemeModeChange(value) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        RadioButton(selected = themeMode == value, onClick = { onThemeModeChange(value) })
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("Done") } }
            }
        }
    }
}




@Composable
fun AboutSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val version = runCatching {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    val githubUrl = "https://github.com/jovertpabon-hub/Schedules"
    val email = "jovertpabon@gmail.com"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)
                ) {
                    // 1. Header — App Identity
                    Text(
                        text = "ABOUT THE APP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "CoffeeLog",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Your academic schedule, brewed into simplicity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "VERSION $version",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 2. App Purpose Section
                    Text(
                        text = "Why CoffeeLog?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "CoffeeLog helps students organize academic schedules, track available time, and manage everyday university routines.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.4f
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 3. Developer Introduction Section
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DEVELOPED BY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "JODev",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Independent Developer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "CoffeeLog began as a personal project to make university scheduling simpler while exploring and learning software development.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4. Contact Section
                    Text(
                        text = "Find me online",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(12.dp))

                    ContactRow(
                        icon = Icons.Default.Code,
                        title = "GitHub",
                        subtitle = "Explore the source code",
                        onClick = { uriHandler.openUri(githubUrl) }
                    )

                    Spacer(Modifier.height(10.dp))

                    ContactRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        subtitle = email,
                        onClick = { uriHandler.openUri("mailto:$email") }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Fixed Footer Action Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Made with care for students.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "© 2026 JODev · CoffeeLog",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutSettingsDialogPreview() {
    SchedulesTheme(themeMode = "light") {
        AboutSettingsDialog(
            onDismiss = {}
        )
    }
}


