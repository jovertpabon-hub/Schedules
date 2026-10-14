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

class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
            room = room,
            instructor = "Instructor",
        )

        // Safety verification check against Database
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (scheduleId != -1L) {
                    val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                    val existing = dao.getScheduleById(scheduleId)
                    if (existing == null) {
                        Log.d("ScheduleAlarmReceiver", "Schedule id=$scheduleId was deleted, cancelling notification dispatch.")
                        return@launch
                    }
                }
                ScheduleNotificationHelper.showClassReminderNotification(context, dummyCourse)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
