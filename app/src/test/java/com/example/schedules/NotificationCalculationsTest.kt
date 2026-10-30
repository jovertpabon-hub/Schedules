package com.example.schedules

import com.example.schedules.data.notifications.NotificationPreferences
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import org.junit.Assert.assertFalse
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

    @Test
    fun testQuietHoursCalculationCrossingMidnight() {
        val start = LocalTime.of(22, 0) // 10:00 PM
        val end = LocalTime.of(7, 0)   // 07:00 AM

        val lateNight = LocalTime.of(23, 30)
        val earlyMorning = LocalTime.of(3, 15)
        val noon = LocalTime.of(12, 0)

        assertTrue("23:30 should be inside Quiet Hours 22:00-07:00", NotificationPreferences.checkTimeInQuietHours(start, end, lateNight))
        assertTrue("03:15 should be inside Quiet Hours 22:00-07:00", NotificationPreferences.checkTimeInQuietHours(start, end, earlyMorning))
        assertFalse("12:00 should NOT be inside Quiet Hours 22:00-07:00", NotificationPreferences.checkTimeInQuietHours(start, end, noon))
    }

    @Test
    fun testQuietHoursCalculationSameDay() {
        val start = LocalTime.of(9, 0)  // 09:00 AM
        val end = LocalTime.of(17, 0)  // 05:00 PM

        val insideWorkHours = LocalTime.of(11, 0)
        val evening = LocalTime.of(18, 0)

        assertTrue("11:00 should be inside Quiet Hours 09:00-17:00", NotificationPreferences.checkTimeInQuietHours(start, end, insideWorkHours))
        assertFalse("18:00 should NOT be inside Quiet Hours 09:00-17:00", NotificationPreferences.checkTimeInQuietHours(start, end, evening))
    }
}

