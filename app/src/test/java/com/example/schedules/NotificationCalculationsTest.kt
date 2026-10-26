package com.example.schedules

import com.example.schedules.data.notifications.ScheduleNotificationHelper
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class NotificationCalculationsTest {

    @Test
    fun testCalculateTargetMillisFutureOccurrence() {
        val now = System.currentTimeMillis()
        val classStartTime = LocalTime.of(10, 0)
        val offsetMinutes = 15

        // Monday 10:00 AM class with 15 min reminder
        val targetMillis = ScheduleNotificationHelper.calculateTargetMillis("Monday", classStartTime, offsetMinutes)

        assertTrue("Target millis should be strictly in the future", targetMillis > now)

        val targetDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(targetMillis), ZoneId.systemDefault())
        // Minute should be 45 (10:00 minus 15 mins)
        assertTrue("Reminder minute should be 45", targetDateTime.minute == 45)
    }

    @Test
    fun testCalculateTargetMillisMidnightCrossing() {
        val now = System.currentTimeMillis()
        // Class at 12:10 AM (00:10)
        val classStartTime = LocalTime.of(0, 10)
        val offsetMinutes = 15

        // 15 minutes before 00:10 AM is 11:55 PM (23:55)
        val targetMillis = ScheduleNotificationHelper.calculateTargetMillis("Monday", classStartTime, offsetMinutes)

        assertTrue("Target millis should be strictly in the future", targetMillis > now)

        val targetDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(targetMillis), ZoneId.systemDefault())
        assertTrue("Reminder hour should be 23 (11 PM)", targetDateTime.hour == 23)
        assertTrue("Reminder minute should be 55", targetDateTime.minute == 55)
    }
}
