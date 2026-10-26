package com.example.schedules.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.schedules.data.local.ScheduleDatabase
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * Handles incoming alarm broadcasts from AlarmManager.
 * Displays class reminder notification and reschedules next weekly occurrence.
 */
class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", -1L)
        val courseCode = intent.getStringExtra("course_code") ?: "Class"
        val courseName = intent.getStringExtra("course_name") ?: "Course"
        val room = intent.getStringExtra("room") ?: "TBA"
        val startTimeStr = intent.getStringExtra("start_time") ?: "08:00 AM"

        val receiveTimeMs = System.currentTimeMillis()
        Log.d("ScheduleAlarmReceiver", "Alarm broadcast received for schedule_id=$scheduleId ($courseCode) at timestamp=$receiveTimeMs")

        val startTime = try {
            LocalTime.parse(startTimeStr, ScheduleConstants.TimeFormatter)
        } catch (_: Exception) {
            LocalTime.now()
        }

        val dummyCourse = CourseClass(
            id = scheduleId,
            courseCode = courseCode,
            courseName = courseName,
            startTime = startTime,
            endTime = startTime.plusHours(1),
            room = room,
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (scheduleId != -1L) {
                    val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                    val existing = dao.getScheduleById(scheduleId)
                    if (existing == null) {
                        Log.d("ScheduleAlarmReceiver", "Schedule id=$scheduleId was deleted from database, skipping notification and recurring schedule.")
                        return@launch
                    }

                    // 1. Post notification
                    ScheduleNotificationHelper.showClassReminderNotification(context, dummyCourse)

                    // 2. Automatically reschedule next weekly occurrence for recurring schedule
                    if (NotificationPreferences.isNotificationsEnabled(context)) {
                        val offsetMinutes = NotificationPreferences.getReminderOffsetMinutes(context)
                        ScheduleNotificationHelper.scheduleExactClassReminder(
                            context,
                            existing,
                            offsetMinutes
                        )
                        Log.d("ScheduleAlarmReceiver", "Rescheduled next weekly reminder for schedule id=${existing.id} (${existing.courseCode})")
                    }
                } else {
                    ScheduleNotificationHelper.showClassReminderNotification(context, dummyCourse)
                }
            } catch (e: Exception) {
                Log.e("ScheduleAlarmReceiver", "Error processing alarm broadcast for schedule id=$scheduleId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
