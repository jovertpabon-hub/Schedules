package com.example.schedules.data.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object NotificationPreferences {
    private const val PREFS_NAME = "schedule_notification_prefs"
    private const val KEY_ENABLED = "notifications_enabled"
    private const val KEY_OFFSET_MINUTES = "reminder_offset_minutes"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_ENABLED, enabled) }
    }

    fun getReminderOffsetMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_OFFSET_MINUTES, 15)
    }

    fun setReminderOffsetMinutes(context: Context, minutes: Int) {
        getPrefs(context).edit { putInt(KEY_OFFSET_MINUTES, minutes) }
    }
}
