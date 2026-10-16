package com.example.schedules.data.parser

import android.util.Log
import com.example.schedules.data.models.ExtractedSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale

class PatternBasedScheduleParser : IScheduleParser {

    companion object {
        private val SUBJECT_REGEX = Regex(
            """^(\S+)\s+(\S+)\s+(.+?)\s+(\d+(?:\.\d+)?)\s+(.+)""",
            RegexOption.IGNORE_CASE
        )

        private val SCHED_SLOT_REGEX = Regex(
            """(\d{1,2}(?::\d{2})?\s*(?:am|pm)?\s*-\s*\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\s+([A-Za-z]+)\s+([A-Za-z0-9\-\s]+)""",
            RegexOption.IGNORE_CASE
        )

        private val TIME_RANGE_REGEX = Regex(
            """(\d{1,2}(?::\d{2})?)\s*(am|pm)?\s*-\s*(\d{1,2}(?::\d{2})?)\s*(am|pm)""",
            RegexOption.IGNORE_CASE
        )

        private val SUFFIX_CLEAN_REGEX = Regex(
            """\s+[A-Z0-9]+\s+[0-9][A-Z]$""",
            RegexOption.IGNORE_CASE
        )

        private val WHITESPACE_REGEX = Regex("""\s+""")

        private val SECTION_CODE_REGEX = Regex(
            """[0-9]?[A-Z]+[0-9]?[A-Z]?""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parse(text: String): List<ExtractedSchedule> {
        val courseClasses = mutableListOf<ExtractedSchedule>()

        var currentCode = ""
        var currentName = ""

        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !isNoiseLine(trimmed)) {
                val subjectMatch = SUBJECT_REGEX.find(trimmed)

                if (subjectMatch != null) {
                    currentCode = subjectMatch.groupValues[2]
                    currentName = subjectMatch.groupValues[3]

                    val rawSchedPart = subjectMatch.groupValues[5]
                    val schedPartClean = rawSchedPart.replace(SUFFIX_CLEAN_REGEX, "").trim()

                    processAndAddSchedule(currentCode, currentName, schedPartClean, courseClasses)
                } else if (currentCode.isNotEmpty()) {
                    processAndAddSchedule(currentCode, currentName, trimmed, courseClasses)
                }
            }
        }

        Log.d("ScheduleParser", "Total parsed classes: ${courseClasses.size}")
        return courseClasses
    }

    private fun processAndAddSchedule(
        code: String,
        name: String,
        textToSearch: String,
        outputList: MutableList<ExtractedSchedule>
    ) {
        val matches = SCHED_SLOT_REGEX.findAll(textToSearch)

        for (match in matches) {
            val timeStr = match.groupValues[1].trim()
            val dayStr = match.groupValues[2].trim()
            var roomPart = match.groupValues[3].trim()

            if (roomPart.contains(" ")) {
                val parts = roomPart.split(WHITESPACE_REGEX)
                if (parts.size > 1 && parts.last().matches(SECTION_CODE_REGEX)) {
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
                        dayOfWeek = day
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
        val match = TIME_RANGE_REGEX.find(timeStr.trim()) ?: return null

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
        val colonIndex = timeStr.indexOf(':')
        var hour = if (colonIndex != -1) timeStr.substring(0, colonIndex).toInt() else timeStr.toInt()
        val minute = if (colonIndex != -1) timeStr.substring(colonIndex + 1).toInt() else 0
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
