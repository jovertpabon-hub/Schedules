package com.example.schedules.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import java.time.LocalTime

class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
            courseCode = courseCode,
            courseName = courseName,
            startTime = startTime,
            endTime = startTime.plusHours(1),
            room = room,
            instructor = "Instructor",
        )

        ScheduleNotificationHelper.showClassReminderNotification(context, dummyCourse)
    }
}
