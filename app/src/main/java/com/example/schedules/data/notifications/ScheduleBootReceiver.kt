package com.example.schedules.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Listens for system reboot and time change broadcasts to recover and reconcile
 * all scheduled class reminders with AlarmManager.
 */
class ScheduleBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("ScheduleBootReceiver", "System broadcast received with action: $action")

        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        )

        if (action in validActions) {
            val pendingResult = goAsync()
            ScheduleNotificationHelper.rescheduleAllReminders(context) {
                Log.d("ScheduleBootReceiver", "Completed alarm recovery/reconciliation after $action")
                pendingResult.finish()
            }
        }
    }
}
