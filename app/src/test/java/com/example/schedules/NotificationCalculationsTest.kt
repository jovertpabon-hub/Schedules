package com.example.schedules

import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.notifications.NotificationContentFormatter
import com.example.schedules.data.notifications.NotificationPreferences
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import org.junit.Assert.assertEquals
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

    @Test
    fun testFormatUpcomingReminderContent() {
        val course = CourseClass(
            id = 1L,
            courseCode = "CS 201",
            courseName = "Data Structures",
            startTime = LocalTime.of(8, 30),
            endTime = LocalTime.of(10, 0),
            room = "Room 204"
        )

        val content = NotificationContentFormatter.formatUpcomingReminder(course, offsetMinutes = 15, showDetails = true)

        assertEquals("Class in 15 min · CS 201", content.title)
        assertEquals("Data Structures · 8:30 AM · Room 204", content.contentText)
        assertEquals("Class in 15 minutes", content.expandedTitle)
        assertTrue(content.expandedText.contains("CS 201 — Data Structures"))
        assertTrue(content.expandedText.contains("Time: 8:30 AM"))
        assertTrue(content.expandedText.contains("Location: Room 204"))
        assertEquals("Upcoming class", content.publicTitle)
    }

    @Test
    fun testFormatClassStartingContent() {
        val course = CourseClass(
            id = 1L,
            courseCode = "CS 201",
            courseName = "Data Structures",
            startTime = LocalTime.of(8, 30),
            endTime = LocalTime.of(10, 0),
            room = "Room 204"
        )

        val content = NotificationContentFormatter.formatClassStarting(course, showDetails = true)

        assertEquals("Class starting · CS 201", content.title)
        assertEquals("Data Structures · Room 204", content.contentText)
        assertEquals("Class starting now", content.expandedTitle)
        assertTrue(content.expandedText.contains("CS 201 — Data Structures"))
        assertTrue(content.expandedText.contains("Scheduled time: 8:30 AM"))
        assertTrue(content.expandedText.contains("Location: Room 204"))
    }

    @Test
    fun testFormatDailySummaryContentWithClasses() {
        val classes = listOf(
            CourseClass(id = 1L, courseCode = "CS 201", courseName = "Data Structures", startTime = LocalTime.of(8, 30), endTime = LocalTime.of(10, 0), room = "Room 204"),
            CourseClass(id = 2L, courseCode = "MATH 201", courseName = "Calculus II", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 30), room = "Hall A"),
            CourseClass(id = 3L, courseCode = "ENG 101", courseName = "English", startTime = LocalTime.of(13, 0), endTime = LocalTime.of(14, 30), room = "Room 102")
        )

        val content = NotificationContentFormatter.formatDailySummary(classes, showDetails = true)

        assertEquals("3 classes today", content.title)
        assertEquals("8:30 CS 201 · 10:00 MATH 201 · +1", content.contentText)
        assertEquals("Your schedule today", content.expandedTitle)
        assertTrue(content.expandedText.contains("3 classes scheduled"))
        assertTrue(content.expandedText.contains("• 8:30 AM — CS 201"))
        assertTrue(content.expandedText.contains("• 10:00 AM — MATH 201"))
        assertTrue(content.expandedText.contains("• 1:00 PM — ENG 101"))
    }

    @Test
    fun testFormatDailySummaryContentNoClasses() {
        val content = NotificationContentFormatter.formatDailySummary(emptyList(), showDetails = true)

        assertEquals("No classes today", content.title)
        assertEquals("Your schedule is clear. Enjoy your free day!", content.contentText)
    }

    @Test
    fun testFormatScheduleChangeContent() {
        val content = NotificationContentFormatter.formatScheduleChange(
            courseCode = "CS 201",
            changeDescription = "Class time changed: 8:30 → 9:00 AM"
        )

        assertEquals("Schedule updated · CS 201", content.title)
        assertEquals("Class time changed: 8:30 → 9:00 AM", content.contentText)
    }

    @Test
    fun testPrivacyDetailsHidden() {
        val course = CourseClass(
            id = 1L,
            courseCode = "CS 201",
            courseName = "Data Structures",
            startTime = LocalTime.of(8, 30),
            endTime = LocalTime.of(10, 0),
            room = "Room 204"
        )

        val content = NotificationContentFormatter.formatUpcomingReminder(course, offsetMinutes = 15, showDetails = false)

        assertEquals("Upcoming class", content.title)
        assertEquals("You have a class starting soon.", content.contentText)
        assertFalse(content.contentText.contains("CS 201"))
        assertFalse(content.contentText.contains("Room 204"))
    }
}
