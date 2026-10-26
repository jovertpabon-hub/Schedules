package com.example.schedules

import com.example.schedules.data.models.ExtractedSchedule
import com.example.schedules.data.models.ScheduleImportPreview
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleImportPreviewTest {

    @Test
    fun testFormatPayloadSize() {
        assertEquals("500 B", ScheduleImportPreview.formatPayloadSize(500L))
        assertEquals("1.00 KB", ScheduleImportPreview.formatPayloadSize(1024L))
        assertEquals("2.50 KB", ScheduleImportPreview.formatPayloadSize(2560L))
        assertEquals("1.50 MB", ScheduleImportPreview.formatPayloadSize(1572864L))
    }

    @Test
    fun testCreateScheduleImportPreview() {
        val sampleText = "CS101 Intro to Programming 8:00am - 10:00am M Room 101"
        val sampleSchedules = listOf(
            ExtractedSchedule(
                courseCode = "CS101",
                courseName = "Intro to Programming",
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                room = "Room 101",
                dayOfWeek = DayOfWeek.MONDAY
            ),
            ExtractedSchedule(
                courseCode = "CS102",
                courseName = "Data Structures",
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(12, 0),
                room = "Room 102",
                dayOfWeek = DayOfWeek.WEDNESDAY
            )
        )

        val preview = ScheduleImportPreview.create(
            schedules = sampleSchedules,
            rawText = sampleText,
            sourceName = "Test PDF"
        )

        assertEquals(2, preview.totalEntries)
        assertEquals(sampleText.length, preview.characterCount)
        assertEquals(sampleText.toByteArray(Charsets.UTF_8).size.toLong(), preview.payloadSizeBytes)
        assertEquals("54 B", preview.payloadSizeFormatted)
        assertEquals("Test PDF", preview.sourceName)
        assertEquals(2, preview.schedules.size)
        assertEquals("CS101", preview.schedules[0].courseCode)
        assertEquals("CS102", preview.schedules[1].courseCode)
    }

    @Test
    fun testEmptyScheduleImportPreview() {
        val preview = ScheduleImportPreview.create(
            schedules = emptyList(),
            rawText = "",
            sourceName = "Empty PDF"
        )

        assertEquals(0, preview.totalEntries)
        assertEquals(0, preview.characterCount)
        assertEquals(0L, preview.payloadSizeBytes)
        assertEquals("0 B", preview.payloadSizeFormatted)
        assertEquals(0, preview.schedules.size)
    }
}
