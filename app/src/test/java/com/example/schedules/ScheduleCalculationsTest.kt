package com.example.schedules

import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.ui.components.calculateTotalClassHours
import com.example.schedules.ui.components.calculateTotalFreeTime
import com.example.schedules.ui.components.calculateVacantTimeBlocks
import com.example.schedules.ui.components.mergedClassIntervals
import com.example.schedules.ui.viewmodel.ScheduleStatusCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ScheduleCalculationsTest {

    private val mondayDate = LocalDate.of(2025, 3, 10) // A Monday

    @Test
    fun testEmptySchedule() {
        val classes = emptyList<CourseClass>()
        val merged = mergedClassIntervals(classes)
        assertEquals(0, merged.size)

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("0 mins", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(1, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 0), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(17, 0), vacantBlocks[0].endTime)
        assertEquals(540L, vacantBlocks[0].durationMinutes)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("9 hrs", totalFreeTime)
    }

    @Test
    fun testSingleClassStandardWindow() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), room = "101")
        )

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("2 hrs", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(2, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 0), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(9, 0), vacantBlocks[0].endTime)
        assertEquals(LocalTime.of(11, 0), vacantBlocks[1].startTime)
        assertEquals(LocalTime.of(17, 0), vacantBlocks[1].endTime)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("7 hrs", totalFreeTime)
    }

    @Test
    fun testBackToBackClasses() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101"),
            CourseClass(courseCode = "CS102", courseName = "Data Structures", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 30), room = "102")
        )

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("2 hr 30 m", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(2, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 0), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(9, 0), vacantBlocks[0].endTime)
        assertEquals(LocalTime.of(11, 30), vacantBlocks[1].startTime)
        assertEquals(LocalTime.of(17, 0), vacantBlocks[1].endTime)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("6 hr 30 m", totalFreeTime)
    }

    @Test
    fun testOverlappingClasses() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Class A", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), room = "101"),
            CourseClass(courseCode = "CS102", courseName = "Class B", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0), room = "102")
        )

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("3 hrs", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(2, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 0), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(9, 0), vacantBlocks[0].endTime)
        assertEquals(LocalTime.of(12, 0), vacantBlocks[1].startTime)
        assertEquals(LocalTime.of(17, 0), vacantBlocks[1].endTime)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("6 hrs", totalFreeTime)
    }

    @Test
    fun testEarlyMorningClass() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Early Bird", startTime = LocalTime.of(7, 0), endTime = LocalTime.of(8, 30), room = "101")
        )

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("1 hr 30 m", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(1, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 30), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(17, 0), vacantBlocks[0].endTime)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("8 hr 30 m", totalFreeTime)
    }

    @Test
    fun testNightClass() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Night Class", startTime = LocalTime.of(17, 30), endTime = LocalTime.of(19, 0), room = "101")
        )

        val totalClassHours = calculateTotalClassHours(classes)
        assertEquals("1 hr 30 m", totalClassHours)

        val vacantBlocks = calculateVacantTimeBlocks(classes)
        assertEquals(1, vacantBlocks.size)
        assertEquals(LocalTime.of(8, 0), vacantBlocks[0].startTime)
        assertEquals(LocalTime.of(17, 30), vacantBlocks[0].endTime)

        val totalFreeTime = calculateTotalFreeTime(vacantBlocks)
        assertEquals("9 hr 30 m", totalFreeTime)
    }

    @Test
    fun testUpcomingClassStatus() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        val status = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(8, 30))

        assertEquals(ClassStatus.UPCOMING, status.status)
        assertNotNull(status.courseClass)
        assertEquals("CS101", status.courseClass?.courseCode)
        assertEquals("00:30:00", status.remainingFormatted)
        assertTrue(status.hasClassesToday)
    }

    @Test
    fun testOngoingClassStatus() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        val status = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(9, 30))

        assertEquals(ClassStatus.ONGOING, status.status)
        assertNotNull(status.courseClass)
        assertEquals("CS101", status.courseClass?.courseCode)
        assertEquals("00:30:00", status.remainingFormatted)
        assertEquals(0.5f, status.progressPercent, 0.01f)
        assertTrue(status.hasClassesToday)
    }

    @Test
    fun testExactEndTimeBoundaryCondition() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        // At 10:00:00 AM exact end time, class MUST NO LONGER be ongoing
        val status = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(10, 0))

        assertEquals(ClassStatus.NONE, status.status)
        assertNull(status.courseClass)
        assertTrue(status.hasClassesToday)
    }

    @Test
    fun testAllClassesCompletedToday() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        val status = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(11, 0))

        assertEquals(ClassStatus.NONE, status.status)
        assertNull(status.courseClass)
        assertTrue(status.hasClassesToday)
    }

    @Test
    fun testNoClassesScheduledToday() {
        val classes = emptyList<CourseClass>()
        val status = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(11, 0))

        assertEquals(ClassStatus.NONE, status.status)
        assertNull(status.courseClass)
        assertFalse(status.hasClassesToday)
    }

    @Test
    fun testCountdownCrossingStartBoundary() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        val beforeStart = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(8, 59, 59))
        assertEquals(ClassStatus.UPCOMING, beforeStart.status)
        assertEquals("00:00:01", beforeStart.remainingFormatted)

        val atStart = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(9, 0, 0))
        assertEquals(ClassStatus.ONGOING, atStart.status)
        assertEquals("01:00:00", atStart.remainingFormatted)
    }

    @Test
    fun testMidnightDateChange() {
        val classes = listOf(
            CourseClass(courseCode = "CS101", courseName = "Intro to CS", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0), room = "101")
        )
        val lateNight = ScheduleStatusCalculator.determineScheduleStatus(classes, mondayDate, LocalTime.of(23, 59, 59))
        assertEquals(ClassStatus.NONE, lateNight.status)
        assertTrue(lateNight.hasClassesToday)

        val nextDayDate = mondayDate.plusDays(1)
        val emptyNextDayStatus = ScheduleStatusCalculator.determineScheduleStatus(emptyList(), nextDayDate, LocalTime.of(0, 0, 0))
        assertEquals(ClassStatus.NONE, emptyNextDayStatus.status)
        assertFalse(emptyNextDayStatus.hasClassesToday)
    }
}
