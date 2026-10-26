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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object ScheduleNotificationHelper {
    private const val CHANNEL_ID = "class_reminders_channel"
    private const val CHANNEL_NAME = "Class Reminders"
    private const val ACTION_SCHEDULE_REMINDER = "com.example.schedules.ACTION_SCHEDULE_REMINDER"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming classes"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("ScheduleNotification", "Notification channel initialized with IMPORTANCE_HIGH")
        }
    }

    fun showClassReminderNotification(context: Context, course: CourseClass) {
        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) {
            Log.w("ScheduleNotification", "Cannot post notification for ${course.courseCode}: POST_NOTIFICATIONS permission not granted")
            return
        }

        val timeStr = course.startTime.format(ScheduleConstants.TimeFormatter)
        val title = "☕ Class Reminder: ${course.courseCode}"
        val message = "${course.courseName} starts at $timeStr in ${course.room}. Don't forget your coffee!"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_coffee)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationId = course.id?.toInt() ?: course.hashCode()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d("ScheduleNotification", "Notification submitted successfully to NotificationManager for ${course.courseCode} (id=${course.id}, notificationId=$notificationId)")
        } catch (e: SecurityException) {
            Log.e("ScheduleNotification", "Security Exception posting notification for ${course.courseCode}", e)
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to post notification for ${course.courseCode}", e)
        }
    }

    fun scheduleExactClassReminder(
        context: Context,
        schedule: ScheduleEntity,
        offsetMinutes: Int
    ) {
        val hasPostNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (!hasPostNotificationPermission) {
            Log.w("ScheduleNotification", "Skipping alarm scheduling for ${schedule.courseCode} (id=${schedule.id}): POST_NOTIFICATIONS permission unavailable")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val targetMillis = calculateTargetMillis(schedule.dayName, schedule.startTime, offsetMinutes)
        val targetDateTimeStr = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(targetMillis),
            ZoneId.systemDefault()
        ).toString()

        val pendingIntent = createReminderPendingIntent(context, schedule)

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                Log.d(
                    "ScheduleNotification",
                    "Exact alarm (setExactAndAllowWhileIdle) registered for ${schedule.courseCode} (id=${schedule.id}) at targetMillis=$targetMillis ($targetDateTimeStr, offset=$offsetMinutes min)"
                )
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                Log.w(
                    "ScheduleNotification",
                    "Exact alarm permission unavailable. Fallback to inexact alarm for ${schedule.courseCode} (id=${schedule.id}) at targetMillis=$targetMillis ($targetDateTimeStr)"
                )
            }
        } catch (e: SecurityException) {
            Log.e("ScheduleNotification", "SecurityException scheduling alarm for ${schedule.courseCode} (id=${schedule.id})", e)
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Failed to schedule alarm for ${schedule.courseCode} (id=${schedule.id})", e)
        }
    }

    fun cancelClassReminder(context: Context, schedule: ScheduleEntity) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = createReminderPendingIntent(context, schedule)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ScheduleNotification", "Cancelled pending alarm for ${schedule.courseCode} (id=${schedule.id})")
        } catch (e: Exception) {
            Log.e("ScheduleNotification", "Error cancelling alarm for ${schedule.courseCode} (id=${schedule.id})", e)
        }
    }

    fun rescheduleAllReminders(context: Context, onComplete: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = NotificationPreferences.isNotificationsEnabled(context)
                val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                val allSchedules = dao.fetchAllSchedulesDirect()
                val offsetMinutes = NotificationPreferences.getReminderOffsetMinutes(context)

                Log.d("ScheduleNotification", "Reconciling reminders for ${allSchedules.size} schedules. Enabled=$isEnabled, Offset=$offsetMinutes min")

                allSchedules.forEach { schedule ->
                    cancelClassReminder(context, schedule)
                    if (isEnabled) {
                        scheduleExactClassReminder(context, schedule, offsetMinutes)
                    }
                }
            } catch (e: Exception) {
                Log.e("ScheduleNotification", "Error during rescheduleAllReminders", e)
            } finally {
                onComplete?.invoke()
            }
        }
    }

    private fun createReminderPendingIntent(context: Context, schedule: ScheduleEntity): PendingIntent {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ACTION_SCHEDULE_REMINDER
            data = "schedule://${schedule.id}".toUri()
            putExtra("schedule_id", schedule.id)
            putExtra("course_code", schedule.courseCode)
            putExtra("course_name", schedule.courseName)
            putExtra("room", schedule.room)
            putExtra("start_time", schedule.startTime.format(ScheduleConstants.TimeFormatter))
            putExtra("day_name", schedule.dayName)
        }
        val requestCode = schedule.id.toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun calculateTargetMillis(dayName: String, classStartTime: java.time.LocalTime, offsetMinutes: Int): Long {
        val targetDayOfWeek = try {
            DayOfWeek.valueOf(dayName.uppercase(Locale.ROOT))
        } catch (_: Exception) {
            DayOfWeek.MONDAY
        }

        val now = LocalDateTime.now()
        // Compute candidate occurrence date for the class
        var classDateTime = now.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
            .withHour(classStartTime.hour)
            .withMinute(classStartTime.minute)
            .withSecond(0)
            .withNano(0)

        // Compute actual reminder target time by subtracting offset from class start
        var reminderDateTime = classDateTime.minusMinutes(offsetMinutes.toLong())

        // If reminder target time is in the past, advance class occurrence by 1 week
        if (!reminderDateTime.isAfter(now)) {
            classDateTime = classDateTime.plusWeeks(1)
            reminderDateTime = classDateTime.minusMinutes(offsetMinutes.toLong())
        }

        return reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
