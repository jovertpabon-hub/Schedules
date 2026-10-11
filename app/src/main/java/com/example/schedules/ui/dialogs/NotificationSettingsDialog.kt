package com.example.schedules.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schedules.data.notifications.NotificationPreferences

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(NotificationPreferences.isNotificationsEnabled(context)) }
    var offsetMinutes by remember { mutableIntStateOf(NotificationPreferences.getReminderOffsetMinutes(context)) }

    val options = listOf(
        0 to "At class start time",
        5 to "5 minutes before",
        15 to "15 minutes before",
        30 to "30 minutes before",
        60 to "1 hour before"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Reminders", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            NotificationPreferences.setNotificationsEnabled(context, it)
                        }
                    )
                }

                Text("Alert Time Before Class:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                options.forEach { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = offsetMinutes == minutes,
                            onClick = {
                                offsetMinutes = minutes
                                NotificationPreferences.setReminderOffsetMinutes(context, minutes)
                            },
                            enabled = enabled
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Save & Apply")
            }
        }
    )
}
