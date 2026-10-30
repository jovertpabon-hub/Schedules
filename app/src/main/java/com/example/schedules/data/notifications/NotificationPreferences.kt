package com.example.schedules.data.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.time.LocalTime

data class NotificationSettingsData(
    val globalEnabled: Boolean = true,
    val defaultOffsetMinutes: Int = 15,
    val upcomingClassesEnabled: Boolean = true,
    val classStartingEnabled: Boolean = true,
    val scheduleChangesEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: LocalTime = LocalTime.of(7, 0),
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: LocalTime = LocalTime.of(22, 0),
    val quietHoursEnd: LocalTime = LocalTime.of(7, 0),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showClassDetails: Boolean = true,
    val hideLockScreenDetails: Boolean = false,
    val schoolDays: Set<String> = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
    val weekendRemindersEnabled: Boolean = false,
    val perClassOffsets: Map<Long, Int> = emptyMap()
)

object NotificationPreferences {
    private const val PREFS_NAME = "schedule_notification_prefs"
    
    private const val KEY_ENABLED = "notifications_enabled"
    private const val KEY_OFFSET_MINUTES = "reminder_offset_minutes"
    
    private const val KEY_UPCOMING_CLASSES = "upcoming_classes_enabled"
    private const val KEY_CLASS_STARTING = "class_starting_enabled"
    private const val KEY_SCHEDULE_CHANGES = "schedule_changes_enabled"
    private const val KEY_DAILY_SUMMARY = "daily_summary_enabled"
    private const val KEY_DAILY_SUMMARY_HOUR = "daily_summary_hour"
    private const val KEY_DAILY_SUMMARY_MINUTE = "daily_summary_minute"

    private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
    private const val KEY_QUIET_HOURS_START_HOUR = "quiet_hours_start_hour"
    private const val KEY_QUIET_HOURS_START_MIN = "quiet_hours_start_min"
    private const val KEY_QUIET_HOURS_END_HOUR = "quiet_hours_end_hour"
    private const val KEY_QUIET_HOURS_END_MIN = "quiet_hours_end_min"

    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_SHOW_CLASS_DETAILS = "show_class_details"
    private const val KEY_HIDE_LOCKSCREEN_DETAILS = "hide_lockscreen_details"

    private const val KEY_SCHOOL_DAYS = "school_days"
    private const val KEY_WEEKEND_REMINDERS = "weekend_reminders_enabled"

    private const val PREFIX_CLASS_OFFSET = "class_offset_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun updateBoolean(context: Context, key: String, default: Boolean, value: Boolean): Boolean {
        val prefs = getPrefs(context)
        if (prefs.getBoolean(key, default) == value) return false
        prefs.edit { putBoolean(key, value) }
        return true
    }

    private fun updateInt(context: Context, key: String, default: Int, value: Int): Boolean {
        val prefs = getPrefs(context)
        if (prefs.getInt(key, default) == value) return false
        prefs.edit { putInt(key, value) }
        return true
    }

    // --- Global Notifications & Class Reminders ---

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_ENABLED, true, enabled)

    fun getReminderOffsetMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_OFFSET_MINUTES, 15)
    }

    fun setReminderOffsetMinutes(context: Context, minutes: Int): Boolean = updateInt(context, KEY_OFFSET_MINUTES, 15, minutes)

    // --- Individual Class Reminder Overrides ---

    fun getClassReminderOffset(context: Context, classId: Long): Int {
        return getPrefs(context).getInt("$PREFIX_CLASS_OFFSET$classId", -1)
    }

    fun setClassReminderOffset(context: Context, classId: Long, offsetMinutes: Int): Boolean = updateInt(context, "$PREFIX_CLASS_OFFSET$classId", -1, offsetMinutes)

    fun getClassEffectiveOffsetMinutes(context: Context, classId: Long): Int {
        val perClass = getClassReminderOffset(context, classId)
        return if (perClass >= 0) perClass else getReminderOffsetMinutes(context)
    }

    fun getAllPerClassOffsets(context: Context): Map<Long, Int> {
        val prefs = getPrefs(context)
        val result = mutableMapOf<Long, Int>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith(PREFIX_CLASS_OFFSET) && value is Int && value >= 0) {
                val classId = key.removePrefix(PREFIX_CLASS_OFFSET).toLongOrNull()
                if (classId != null) {
                    result[classId] = value
                }
            }
        }
        return result
    }

    // --- Notification Categories ---

    fun isUpcomingClassesEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_UPCOMING_CLASSES, true)
    }

    fun setUpcomingClassesEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_UPCOMING_CLASSES, true, enabled)

    fun isClassStartingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CLASS_STARTING, true)
    }

    fun setClassStartingEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_CLASS_STARTING, true, enabled)

    fun isScheduleChangesEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCHEDULE_CHANGES, true)
    }

    fun setScheduleChangesEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_SCHEDULE_CHANGES, true, enabled)

    fun isDailySummaryEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DAILY_SUMMARY, false)
    }

    fun setDailySummaryEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_DAILY_SUMMARY, false, enabled)

    fun getDailySummaryTime(context: Context): LocalTime {
        val prefs = getPrefs(context)
        val hour = prefs.getInt(KEY_DAILY_SUMMARY_HOUR, 7)
        val min = prefs.getInt(KEY_DAILY_SUMMARY_MINUTE, 0)
        return LocalTime.of(hour, min)
    }

    fun setDailySummaryTime(context: Context, hour: Int, minute: Int): Boolean {
        val current = getDailySummaryTime(context)
        val requested = LocalTime.of(hour, minute)
        if (current == requested) return false
        getPrefs(context).edit {
            putInt(KEY_DAILY_SUMMARY_HOUR, hour)
            putInt(KEY_DAILY_SUMMARY_MINUTE, minute)
        }
        return true
    }

    // --- Quiet Hours ---

    fun isQuietHoursEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_QUIET_HOURS_ENABLED, false)
    }

    fun setQuietHoursEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_QUIET_HOURS_ENABLED, false, enabled)

    fun getQuietHoursStart(context: Context): LocalTime {
        val prefs = getPrefs(context)
        val hour = prefs.getInt(KEY_QUIET_HOURS_START_HOUR, 22)
        val min = prefs.getInt(KEY_QUIET_HOURS_START_MIN, 0)
        return LocalTime.of(hour, min)
    }

    fun setQuietHoursStart(context: Context, hour: Int, minute: Int): Boolean {
        val requested = LocalTime.of(hour, minute)
        if (getQuietHoursStart(context) == requested) return false
        getPrefs(context).edit {
            putInt(KEY_QUIET_HOURS_START_HOUR, hour)
            putInt(KEY_QUIET_HOURS_START_MIN, minute)
        }
        return true
    }

    fun getQuietHoursEnd(context: Context): LocalTime {
        val prefs = getPrefs(context)
        val hour = prefs.getInt(KEY_QUIET_HOURS_END_HOUR, 7)
        val min = prefs.getInt(KEY_QUIET_HOURS_END_MIN, 0)
        return LocalTime.of(hour, min)
    }

    fun setQuietHoursEnd(context: Context, hour: Int, minute: Int): Boolean {
        val requested = LocalTime.of(hour, minute)
        if (getQuietHoursEnd(context) == requested) return false
        getPrefs(context).edit {
            putInt(KEY_QUIET_HOURS_END_HOUR, hour)
            putInt(KEY_QUIET_HOURS_END_MIN, minute)
        }
        return true
    }

    fun isInQuietHours(context: Context, time: LocalTime = LocalTime.now()): Boolean {
        if (!isQuietHoursEnabled(context)) return false
        val start = getQuietHoursStart(context)
        val end = getQuietHoursEnd(context)
        return checkTimeInQuietHours(start, end, time)
    }

    fun checkTimeInQuietHours(start: LocalTime, end: LocalTime, time: LocalTime): Boolean {
        return if (start.isAfter(end)) {
            time >= start || time <= end
        } else if (start == end) {
            true
        } else {
            time >= start && time <= end
        }
    }

    // --- Notification Behavior ---

    fun isSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_SOUND_ENABLED, true, enabled)

    fun isVibrationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_VIBRATION_ENABLED, true, enabled)

    fun showClassDetails(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_CLASS_DETAILS, true)
    }

    fun setShowClassDetails(context: Context, show: Boolean): Boolean = updateBoolean(context, KEY_SHOW_CLASS_DETAILS, true, show)

    fun hideLockScreenDetails(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HIDE_LOCKSCREEN_DETAILS, false)
    }

    fun setHideLockScreenDetails(context: Context, hide: Boolean): Boolean = updateBoolean(context, KEY_HIDE_LOCKSCREEN_DETAILS, false, hide)

    // --- Schedule Preferences ---

    fun getSchoolDays(context: Context): Set<String> {
        val defaultDays = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        return getPrefs(context).getStringSet(KEY_SCHOOL_DAYS, defaultDays) ?: defaultDays
    }

    fun setSchoolDays(context: Context, days: Set<String>): Boolean {
        if (getSchoolDays(context) == days) return false
        getPrefs(context).edit { putStringSet(KEY_SCHOOL_DAYS, days) }
        return true
    }

    fun isWeekendRemindersEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WEEKEND_REMINDERS, false)
    }

    fun setWeekendRemindersEnabled(context: Context, enabled: Boolean): Boolean = updateBoolean(context, KEY_WEEKEND_REMINDERS, false, enabled)

    // --- Load Snapshot ---

    fun loadAllSettings(context: Context): NotificationSettingsData {
        return NotificationSettingsData(
            globalEnabled = isNotificationsEnabled(context),
            defaultOffsetMinutes = getReminderOffsetMinutes(context),
            upcomingClassesEnabled = isUpcomingClassesEnabled(context),
            classStartingEnabled = isClassStartingEnabled(context),
            scheduleChangesEnabled = isScheduleChangesEnabled(context),
            dailySummaryEnabled = isDailySummaryEnabled(context),
            dailySummaryTime = getDailySummaryTime(context),
            quietHoursEnabled = isQuietHoursEnabled(context),
            quietHoursStart = getQuietHoursStart(context),
            quietHoursEnd = getQuietHoursEnd(context),
            soundEnabled = isSoundEnabled(context),
            vibrationEnabled = isVibrationEnabled(context),
            showClassDetails = showClassDetails(context),
            hideLockScreenDetails = hideLockScreenDetails(context),
            schoolDays = getSchoolDays(context),
            weekendRemindersEnabled = isWeekendRemindersEnabled(context),
            perClassOffsets = getAllPerClassOffsets(context)
        )
    }
}

