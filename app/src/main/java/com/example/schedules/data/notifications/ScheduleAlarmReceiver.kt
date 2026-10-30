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
import java.time.LocalDate
import java.time.LocalTime

/**
 * Handles incoming alarm broadcasts from AlarmManager.
 * Displays class reminder, class starting, and daily summary notifications,
 * and automatically reschedules recurring occurrences.
 */
class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ScheduleNotificationHelper.ACTION_SCHEDULE_REMINDER
        val receiveTimeMs = System.currentTimeMillis()
        Log.d("ScheduleAlarmReceiver", "Alarm broadcast received action=$action at timestamp=$receiveTimeMs")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()

                if (action == ScheduleNotificationHelper.ACTION_DAILY_SUMMARY) {
                    val todayName = LocalDate.now().dayOfWeek.name
                    val dayNameFormatted = ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(todayName, ignoreCase = true) } ?: todayName
                    val todayEntities = dao.getSchedulesForDayDirect(dayNameFormatted)
                    val todayClasses = todayEntities.map {
                        CourseClass(
                            id = it.id,
                            courseCode = it.courseCode,
                            courseName = it.courseName,
                            startTime = it.startTime,
                            endTime = it.endTime,
                            room = it.room
                        )
                    }.sortedBy { it.startTime }

                    ScheduleNotificationHelper.showDailySummaryNotification(context, todayClasses)
                    ScheduleNotificationHelper.scheduleDailySummaryAlarm(context)
                    return@launch
                }

                val scheduleId = intent.getLongExtra("schedule_id", -1L)
                val courseCode = intent.getStringExtra("course_code") ?: "Class"
                val courseName = intent.getStringExtra("course_name") ?: "Course"
                val room = intent.getStringExtra("room") ?: "TBA"
                val startTimeStr = intent.getStringExtra("start_time") ?: "08:00 AM"

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
                    room = room
                )

                if (scheduleId != -1L) {
                    val existing = dao.getScheduleById(scheduleId)
                    if (existing == null) {
                        Log.d("ScheduleAlarmReceiver", "Schedule id=$scheduleId was deleted, skipping broadcast")
                        return@launch
                    }

                    val isStarting = action == ScheduleNotificationHelper.ACTION_CLASS_STARTING
                    ScheduleNotificationHelper.showClassReminderNotification(
                        context = context,
                        course = dummyCourse,
                        isClassStarting = isStarting
                    )

                    // Reschedule next weekly occurrence
                    if (NotificationPreferences.isNotificationsEnabled(context)) {
                        ScheduleNotificationHelper.scheduleExactClassReminder(context, existing)
                    }
                } else {
                    ScheduleNotificationHelper.showClassReminderNotification(context, dummyCourse)
                }
            } catch (e: Exception) {
                Log.e("ScheduleAlarmReceiver", "Error processing alarm broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

