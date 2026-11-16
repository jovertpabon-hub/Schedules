package com.example.schedules.data.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.example.schedules.R
import com.example.schedules.data.local.ScheduleDatabase
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object ScheduleNotificationHelper {
    const val CHANNEL_ID = "class_reminders_channel"
    private const val CHANNEL_NAME = "Class Reminders"

    const val CHANNEL_SILENT_ID = "silent_reminders_channel"
    private const val CHANNEL_SILENT_NAME = "Silent Reminders (Quiet Hours)"

    const val CHANNEL_UPDATES_ID = "schedule_updates_channel"
    private const val CHANNEL_UPDATES_NAME = "Schedule Updates"

    const val CHANNEL_SUMMARY_ID = "daily_summary_channel"
    private const val CHANNEL_SUMMARY_NAME = "Daily Summaries"

    const val ACTION_SCHEDULE_REMINDER = "com.example.schedules.ACTION_SCHEDULE_REMINDER"
    const val ACTION_CLASS_STARTING = "com.example.schedules.ACTION_CLASS_STARTING"
    const val ACTION_DAILY_SUMMARY = "com.example.schedules.ACTION_DAILY_SUMMARY"

    private const val DAILY_SUMMARY_REQUEST_CODE = 999999

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming classes"
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT_ID,
                CHANNEL_SILENT_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent class notifications delivered during quiet hours"
                setSound(null, null)
                enableVibration(false)
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES_ID,
                CHANNEL_UPDATES_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when class schedules are modified"
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY_ID,
                CHANNEL_SUMMARY_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning summary of scheduled classes"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(silentChannel)
            notificationManager.createNotificationChannel(updatesChannel)
            notificationManager.createNotificationChannel(summaryChannel)
            Log.d("ScheduleNotification", "All ParClock notification channels initialized successfully")
        }
    }

    fun showClassReminderNotification(
        context: Context,
        course: CourseClass,
        isClassStarting: Boolean = false,
        offsetMinutes: Int = 15
    ) {
        if (!NotificationPreferences.isNotificationsEnabled(context)) return

        if (isClassStarting && !NotificationPreferences.isClassStartingEnabled(context)) return
        if (!isClassStarting && !NotificationPreferences.isUpcomingClassesEnabled(context)) return

        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) {
            Log.w("ScheduleNotification", "Cannot post notification for ${course.courseCode}: POST_NOTIFICATIONS permission not granted")
            return
        }

        val inQuietHours = NotificationPreferences.isInQuietHours(context)
        val showDetails = NotificationPreferences.showClassDetails(context)
        val hideLockScreen = NotificationPreferences.hideLockScreenDetails(context)

        val targetChannelId = if (inQuietHours) CHANNEL_SILENT_ID else CHANNEL_ID

        val formattedContent = if (isClassStarting) {
            NotificationContentFormatter.formatClassStarting(course, showDetails)
        } else {
            NotificationContentFormatter.formatUpcomingReminder(course, offsetMinutes, showDetails)
        }

        val largeIcon = ContextCompat.getDrawable(context, R.drawable.clofee_icon)?.toBitmap()
        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(R.drawable.clofee_icon)
            .setLargeIcon(largeIcon)
            .setContentTitle(formattedContent.title)
            .setContentText(formattedContent.contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(formattedContent.expandedTitle)
                    .bigText(formattedContent.expandedText)
            )
            .setAutoCancel(true)

        val publicBuilder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(R.drawable.clofee_icon)
            .setContentTitle(formattedContent.publicTitle)
            .setContentText(formattedContent.publicContentText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setPublicVersion(publicBuilder.build())

        if (hideLockScreen) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        if (inQuietHours) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
            builder.setSound(null)
            builder.setVibrate(longArrayOf(0))
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)

            if (!NotificationPreferences.isSoundEnabled(context)) {
                builder.setSound(null)
            }
            if (!NotificationPreferences.isVibrationEnabled(context)) {
                builder.setVibrate(longArrayOf(0))
            }
        }

        try {
            val notificationId = (course.id?.toInt() ?: course.hashCode()) + if (isClassStarting) 500000 else 0
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d("ScheduleNotification", "Notification submitted successfully (id=$notificationId, isClassStarting=$isClassStarting, quietHours=$inQuietHours)")
        } catch (e: SecurityException) {
            Log.e("ScheduleNotification", "Security Exception posting notification for ${course.courseCode}", e)
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to post notification for ${course.courseCode}", e)
        }
    }

    @JvmName("showScheduleChangeNotificationWithCourseCode")
    fun showScheduleChangeNotification(
        context: Context,
        courseCode: String?,
        changeDescription: String
    ) {
        if (!NotificationPreferences.isNotificationsEnabled(context)) return
        if (!NotificationPreferences.isScheduleChangesEnabled(context)) return

        if (NotificationPreferences.isInQuietHours(context)) {
            Log.d("ScheduleNotification", "Schedule change notification suppressed due to Quiet Hours.")
            return
        }

        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) return

        val formattedContent = NotificationContentFormatter.formatScheduleChange(courseCode, changeDescription)

        val largeIcon = ContextCompat.getDrawable(context, R.drawable.clofee_icon)?.toBitmap()
        val builder = NotificationCompat.Builder(context, CHANNEL_UPDATES_ID)
            .setSmallIcon(R.drawable.clofee_icon)
            .setLargeIcon(largeIcon)
            .setContentTitle(formattedContent.title)
            .setContentText(formattedContent.contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(formattedContent.expandedTitle)
                    .bigText(formattedContent.expandedText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val publicBuilder = NotificationCompat.Builder(context, CHANNEL_UPDATES_ID)
            .setSmallIcon(R.drawable.clofee_icon)
            .setContentTitle(formattedContent.publicTitle)
            .setContentText(formattedContent.publicContentText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setPublicVersion(publicBuilder.build())

        if (NotificationPreferences.hideLockScreenDetails(context)) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        if (!NotificationPreferences.isSoundEnabled(context)) builder.setSound(null)
        if (!NotificationPreferences.isVibrationEnabled(context)) builder.setVibrate(longArrayOf(0))

        try {
            val notificationId = (System.currentTimeMillis() % 100000).toInt() + 800000
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to post schedule change notification", e)
        }
    }

    fun showScheduleChangeNotification(context: Context, message: String) {
        showScheduleChangeNotification(context, courseCode = null, changeDescription = message)
    }

    fun showDailySummaryNotification(context: Context, todayClasses: List<CourseClass>) {
        if (!NotificationPreferences.isNotificationsEnabled(context)) return
        if (!NotificationPreferences.isDailySummaryEnabled(context)) return

        if (NotificationPreferences.isInQuietHours(context)) {
            Log.d("ScheduleNotification", "Daily summary notification suppressed due to Quiet Hours.")
            return
        }

        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) return

        val showDetails = NotificationPreferences.showClassDetails(context)
        val formattedContent = NotificationContentFormatter.formatDailySummary(todayClasses, showDetails)

        val largeIcon = ContextCompat.getDrawable(context, R.drawable.clofee_icon)?.toBitmap()
        val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARY_ID)
            .setSmallIcon(R.drawable.clofee_icon)
            .setLargeIcon(largeIcon)
            .setContentTitle(formattedContent.title)
            .setContentText(formattedContent.contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(formattedContent.expandedTitle)
                    .bigText(formattedContent.expandedText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val publicBuilder = NotificationCompat.Builder(context, CHANNEL_SUMMARY_ID)
            .setSmallIcon(R.drawable.clofee_icon)
            .setContentTitle(formattedContent.publicTitle)
            .setContentText(formattedContent.publicContentText)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setPublicVersion(publicBuilder.build())

        if (NotificationPreferences.hideLockScreenDetails(context)) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        if (!NotificationPreferences.isSoundEnabled(context)) builder.setSound(null)
        if (!NotificationPreferences.isVibrationEnabled(context)) builder.setVibrate(longArrayOf(0))

        try {
            NotificationManagerCompat.from(context).notify(DAILY_SUMMARY_REQUEST_CODE, builder.build())
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to post daily summary notification", e)
        }
    }

    fun scheduleExactClassReminder(
        context: Context,
        schedule: ScheduleEntity,
        overrideOffsetMinutes: Int? = null
    ) {
        cancelClassReminder(context, schedule)
        if (!NotificationPreferences.isNotificationsEnabled(context)) return

        val dayOfWeekName = schedule.dayName
        val schoolDays = NotificationPreferences.getSchoolDays(context)

        // Check if day is part of user's selected school days
        val isDayInSchoolDays = schoolDays.any { it.equals(dayOfWeekName, ignoreCase = true) }
        if (!isDayInSchoolDays) {
            Log.d("ScheduleNotification", "Skipping alarm for ${schedule.courseCode}: $dayOfWeekName is not in selected school days")
            cancelClassReminder(context, schedule)
            return
        }

        // Check weekend reminders preference
        val targetDayOfWeek = try { DayOfWeek.valueOf(dayOfWeekName.uppercase(Locale.ROOT)) } catch (_: Exception) { DayOfWeek.MONDAY }
        val isWeekend = targetDayOfWeek == DayOfWeek.SATURDAY || targetDayOfWeek == DayOfWeek.SUNDAY
        if (isWeekend && !NotificationPreferences.isWeekendRemindersEnabled(context)) {
            Log.d("ScheduleNotification", "Skipping alarm for ${schedule.courseCode}: Weekend reminders disabled")
            cancelClassReminder(context, schedule)
            return
        }

        val offsetMinutes = overrideOffsetMinutes ?: NotificationPreferences.getClassEffectiveOffsetMinutes(context, schedule.id)

        // 1. Upcoming class reminder alarm
        if (NotificationPreferences.isUpcomingClassesEnabled(context)) {
            scheduleAlarmForAction(
                context = context,
                schedule = schedule,
                action = ACTION_SCHEDULE_REMINDER,
                offsetMinutes = offsetMinutes,
                requestCode = schedule.id.toInt()
            )
        }

        // 2. Class starting alarm (0 min offset)
        if (NotificationPreferences.isClassStartingEnabled(context)) {
            scheduleAlarmForAction(
                context = context,
                schedule = schedule,
                action = ACTION_CLASS_STARTING,
                offsetMinutes = 0,
                requestCode = schedule.id.toInt() + 500000
            )
        }
    }

    private fun scheduleAlarmForAction(
        context: Context,
        schedule: ScheduleEntity,
        action: String,
        offsetMinutes: Int,
        requestCode: Int
    ) {
        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val targetMillis = calculateTargetMillis(schedule.dayName, schedule.startTime, offsetMinutes)

        val pendingIntent = createAlarmPendingIntent(context, schedule, action, requestCode, offsetMinutes)

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to schedule alarm $action for ${schedule.courseCode}", e)
        }
    }

    fun scheduleDailySummaryAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createDailySummaryPendingIntent(context)

        if (!NotificationPreferences.isNotificationsEnabled(context) || !NotificationPreferences.isDailySummaryEnabled(context)) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            return
        }

        val summaryTime = NotificationPreferences.getDailySummaryTime(context)
        val now = LocalDateTime.now()
        var targetDateTime = now.withHour(summaryTime.hour).withMinute(summaryTime.minute).withSecond(0).withNano(0)

        if (!targetDateTime.isAfter(now)) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d("ScheduleNotification", "Daily summary alarm scheduled for $targetDateTime")
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to schedule daily summary alarm", e)
        }
    }

    fun cancelClassReminder(context: Context, schedule: ScheduleEntity) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val reminderPendingIntent = createAlarmPendingIntent(context, schedule, ACTION_SCHEDULE_REMINDER, schedule.id.toInt())
            alarmManager.cancel(reminderPendingIntent)
            reminderPendingIntent.cancel()

            val startingPendingIntent = createAlarmPendingIntent(context, schedule, ACTION_CLASS_STARTING, schedule.id.toInt() + 500000)
            alarmManager.cancel(startingPendingIntent)
            startingPendingIntent.cancel()

            Log.d("ScheduleNotification", "Cancelled pending alarms for ${schedule.courseCode} (id=${schedule.id})")
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Error cancelling alarm for ${schedule.courseCode} (id=${schedule.id})", e)
        }
    }

    fun rescheduleAllReminders(context: Context, onComplete: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                createNotificationChannel(context)

                val isEnabled = NotificationPreferences.isNotificationsEnabled(context)
                val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                val allSchedules = dao.fetchAllSchedulesDirect()

                Log.d("ScheduleNotification", "Reconciling reminders for ${allSchedules.size} schedules. Enabled=$isEnabled")

                allSchedules.forEach { schedule ->
                    cancelClassReminder(context, schedule)
                    if (isEnabled) {
                        scheduleExactClassReminder(context, schedule)
                    }
                }

                scheduleDailySummaryAlarm(context)
            } catch (e: Exception) {
                Log.e("ScheduleNotification", "Error during rescheduleAllReminders", e)
            } finally {
                onComplete?.invoke()
            }
        }
    }

    private fun createAlarmPendingIntent(
        context: Context,
        schedule: ScheduleEntity,
        actionString: String,
        requestCode: Int,
        offsetMinutes: Int = 15
    ): PendingIntent {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = actionString
            data = "schedule://${schedule.id}/$requestCode".toUri()
            putExtra("schedule_id", schedule.id)
            putExtra("course_code", schedule.courseCode)
            putExtra("course_name", schedule.courseName)
            putExtra("room", schedule.room)
            putExtra("start_time", schedule.startTime.format(ScheduleConstants.TimeFormatter))
            putExtra("day_name", schedule.dayName)
            putExtra("offset_minutes", offsetMinutes)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDailySummaryPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_SUMMARY
            data = "schedule://daily_summary".toUri()
        }
        return PendingIntent.getBroadcast(
            context,
            DAILY_SUMMARY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun calculateTargetMillis(dayName: String, classStartTime: LocalTime, offsetMinutes: Int): Long {
        val targetDayOfWeek = try {
            DayOfWeek.valueOf(dayName.uppercase(Locale.ROOT))
        } catch (_: Exception) {
            DayOfWeek.MONDAY
        }

        val now = LocalDateTime.now()
        var classDateTime = now.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
            .withHour(classStartTime.hour)
            .withMinute(classStartTime.minute)
            .withSecond(0)
            .withNano(0)

        var reminderDateTime = classDateTime.minusMinutes(offsetMinutes.toLong())

        if (!reminderDateTime.isAfter(now)) {
            classDateTime = classDateTime.plusWeeks(1)
            reminderDateTime = classDateTime.minusMinutes(offsetMinutes.toLong())
        }

        return reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

