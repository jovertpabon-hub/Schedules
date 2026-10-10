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
import com.example.schedules.R
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object ScheduleNotificationHelper {
    private const val CHANNEL_ID = "class_reminders_channel"
    private const val CHANNEL_NAME = "Class Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming classes and schedule reminders"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showClassReminderNotification(context: Context, course: CourseClass) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
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
            NotificationManagerCompat.from(context).notify(course.hashCode(), builder.build())
        } catch (e: SecurityException) {
            Log.e("ScheduleNotification", "Security exception posting notification", e)
        }
    }

    fun scheduleExactClassReminder(
        context: Context,
        schedule: ScheduleEntity,
        adjustedStartTime: java.time.LocalTime,
        offsetMinutes: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val targetMillis = calculateTargetMillis(schedule.dayName, adjustedStartTime)
        if (targetMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            putExtra("course_code", schedule.courseCode)
            putExtra("course_name", schedule.courseName)
            putExtra("room", schedule.room)
            putExtra("start_time", schedule.startTime.format(ScheduleConstants.TimeFormatter))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d(
                "ScheduleNotification",
                "Exact alarm scheduled for ${schedule.courseCode} at millis $targetMillis ($offsetMinutes mins before start time ${schedule.startTime})"
            )
        } catch (e: SecurityException) {
            Log.e("ScheduleNotification", "Failed to schedule exact alarm", e)
        }
    }

    fun cancelClassReminder(context: Context, schedule: ScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduleAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("ScheduleNotification", "Cancelled alarm for ${schedule.courseCode}")
    }

    private fun calculateTargetMillis(dayName: String, time: java.time.LocalTime): Long {
        val targetDayOfWeek = try {
            DayOfWeek.valueOf(dayName.uppercase(Locale.ROOT))
        } catch (_: Exception) {
            DayOfWeek.MONDAY
        }

        val now = LocalDateTime.now()
        var targetDate = now.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (targetDate.isBefore(now)) {
            targetDate = targetDate.plusWeeks(1)
        }

        return targetDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
