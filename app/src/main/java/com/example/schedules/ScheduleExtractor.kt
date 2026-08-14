package com.example.schedules

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale

// ==========================================
// DATA MODELS
// ==========================================

data class ExtractedSchedule(
    val courseCode: String,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String = "TBA",
    val dayOfWeek: DayOfWeek,
    val instructor: String = "Unknown"
)

// ==========================================
// SCHEDULE EXTRACTOR (PDF Reader + Parser)
// ==========================================

object ScheduleExtractor {

    // 1. FLEXIBLE SUBJECT REGEX:
    // Group 1: Internal Code (any non-space token, e.g., "C632", "101", "SEC-A")
    // Group 2: Subject Code (any non-space token, e.g., "GE6", "CS-101", "PATHFIT3")
    // Group 3: Description (text before units)
    // Group 4: Units (matches "3.0", "3", "1.5")
    // Group 5: Rest of line (schedule, room, section)
    private val subjectRegex = Regex(
        """^(\S+)\s+(\S+)\s+(.+?)\s+(\d+(?:\.\d+)?)\s+(.+)""",
        RegexOption.IGNORE_CASE
    )

    // 2. FLEXIBLE SCHEDULE SLOT REGEX:
    // Matches time range (e.g., "8-11 am", "10:30 am-12:00 pm"), day (e.g., "M", "T", "Th"), and room (allows spaces/dashes)
    private val schedSlotRegex = Regex(
        """(\d{1,2}(?::\d{2})?\s*(?:am|pm)?\s*-\s*\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\s+([A-Za-z]+)\s+([A-Za-z0-9\-\s]+)""",
        RegexOption.IGNORE_CASE
    )

    private val timeRangeRegex = Regex(
        """(\d{1,2}(?::\d{2})?)\s*(am|pm)?\s*-\s*(\d{1,2}(?::\d{2})?)\s*(am|pm)""",
        RegexOption.IGNORE_CASE
    )

    suspend fun extractTextFromPdf(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(context)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { pdDocument ->
                    val stripper = PDFTextStripper()
                    stripper.sortByPosition = true
                    val text = stripper.getText(pdDocument)
                    Log.d("ScheduleExtractor", "Extracted Text Length: ${text.length}")
                    text
                }
            } ?: ""
        } catch (e: Exception) {
            Log.e("ScheduleExtractor", "Error reading PDF", e)
            ""
        }
    }

    fun parsePdfToExtractedSchedules(extractedText: String): List<ExtractedSchedule> {
        val courseClasses = mutableListOf<ExtractedSchedule>()

        var currentCode = ""
        var currentName = ""

        for (line in extractedText.lines()) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || isNoiseLine(trimmed)) continue

            val subjectMatch = subjectRegex.find(trimmed)

            if (subjectMatch != null) {
                // Main course row found
                currentCode = subjectMatch.groupValues[2] // Subject Code (e.g., "GE6", "PATHFIT3")
                currentName = subjectMatch.groupValues[3]  // Description (e.g., "Art Appreciation")

                val rawSchedPart = subjectMatch.groupValues[5]

                // Clean off section tag at line end if present (e.g., "BSCS 2B")
                val schedPartClean = rawSchedPart.replace(Regex("""\s+[A-Z0-9]+\s+[0-9][A-Z]$""", RegexOption.IGNORE_CASE), "").trim()

                processAndAddSchedule(currentCode, currentName, schedPartClean, courseClasses)

            } else if (currentCode.isNotEmpty()) {
                // Continuation line for multi-slot schedules
                processAndAddSchedule(currentCode, currentName, trimmed, courseClasses)
            }
        }

        Log.d("ScheduleExtractor", "Total parsed classes: ${courseClasses.size}")
        return courseClasses
    }

    private fun processAndAddSchedule(
        code: String,
        name: String,
        textToSearch: String,
        outputList: MutableList<ExtractedSchedule>
    ) {
        val matches = schedSlotRegex.findAll(textToSearch)

        for (match in matches) {
            val timeStr = match.groupValues[1].trim()
            val dayStr = match.groupValues[2].trim()
            var roomPart = match.groupValues[3].trim()

            // Trim trailing section suffix if room accidentally captured section (e.g., "IITRM7 BSCS 2B" -> "IITRM7")
            if (roomPart.contains(" ")) {
                val parts = roomPart.split(Regex("""\s+"""))
                if (parts.size > 1 && parts.last().matches(Regex("""[0-9]?[A-Z]+[0-9]?[A-Z]?""", RegexOption.IGNORE_CASE))) {
                    roomPart = parts.dropLast(1).joinToString(" ")
                }
            }

            val timeRange = parseTimeRange(timeStr) ?: continue
            val dayOfWeekList = parseDayOfWeek(dayStr)

            for (day in dayOfWeekList) {
                outputList.add(
                    ExtractedSchedule(
                        courseCode = code,
                        courseName = name,
                        startTime = timeRange.first,
                        endTime = timeRange.second,
                        room = roomPart.ifEmpty { "TBA" },
                        dayOfWeek = day,
                        instructor = "Unknown"
                    )
                )
            }
        }
    }

    private fun parseDayOfWeek(dayStr: String): List<DayOfWeek> {
        val result = mutableListOf<DayOfWeek>()
        val normalized = dayStr.uppercase(Locale.ROOT)

        if (normalized.contains("TH")) {
            result.add(DayOfWeek.THURSDAY)
        }
        if (normalized.contains("M")) result.add(DayOfWeek.MONDAY)
        if (normalized.contains("T") && !normalized.contains("TH")) result.add(DayOfWeek.TUESDAY)
        if (normalized.contains("W")) result.add(DayOfWeek.WEDNESDAY)
        if (normalized.contains("F")) result.add(DayOfWeek.FRIDAY)
        if (normalized.contains("SA")) {
            result.add(DayOfWeek.SATURDAY)
        } else if (normalized.contains("S") && !normalized.contains("SU") && !normalized.contains("TH")) {
            result.add(DayOfWeek.SATURDAY)
        }
        if (normalized.contains("SU")) result.add(DayOfWeek.SUNDAY)

        return if (result.isEmpty()) listOf(DayOfWeek.SUNDAY) else result
    }

    private fun parseTimeRange(timeStr: String): Pair<LocalTime, LocalTime>? {
        val match = timeRangeRegex.find(timeStr.trim()) ?: return null

        val startHourRaw = match.groupValues[1]
        val startAmPmRaw = match.groupValues[2].ifEmpty { null }
        val endHourRaw = match.groupValues[3]
        val endAmPmRaw = match.groupValues[4]

        val startHourInt = startHourRaw.split(":")[0].toIntOrNull() ?: 0
        val endHourInt = endHourRaw.split(":")[0].toIntOrNull() ?: 0

        val startAmPm = when {
            startAmPmRaw != null -> startAmPmRaw
            endAmPmRaw.equals("pm", ignoreCase = true) && endHourInt == 12 && startHourInt in 8..11 -> "am"
            endAmPmRaw.equals("pm", ignoreCase = true) && startHourInt > endHourInt -> "am"
            else -> endAmPmRaw
        }

        val startTime = convertToLocalTime(startHourRaw, startAmPm)
        val endTime = convertToLocalTime(endHourRaw, endAmPmRaw)

        return Pair(startTime, endTime)
    }

    private fun convertToLocalTime(timeStr: String, amPm: String): LocalTime {
        val parts = timeStr.split(":")
        var hour = parts[0].toInt()
        val minute = if (parts.size > 1) parts[1].toInt() else 0
        val isPm = amPm.equals("pm", ignoreCase = true)

        if (isPm && hour < 12) hour += 12
        if (!isPm && hour == 12) hour = 0

        return LocalTime.of(hour, minute)
    }

    private fun isNoiseLine(line: String): Boolean {
        val lower = line.lowercase(Locale.ROOT)
        return lower.contains("republic of the philippines") ||
                lower.contains("partido state university") ||
                lower.contains("certificate of enrollment") ||
                lower.contains("this is to acknowledge") ||
                lower.contains("signature over") ||
                lower.contains("processed by") ||
                lower.contains("total units:")
    }
}