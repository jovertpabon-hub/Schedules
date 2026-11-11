package com.example.schedules.ui.now.utils

import com.example.schedules.data.models.CourseClass
import com.example.schedules.ui.now.model.VacantTimeBlock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Merges overlapping or contiguous class time intervals into a list of non-overlapping time spans.
 */
fun mergedClassIntervals(classes: List<CourseClass>): List<Pair<LocalTime, LocalTime>> {
    if (classes.isEmpty()) return emptyList()

    val sorted = classes.sortedWith(compareBy({ it.startTime }, { it.endTime }))
    val merged = mutableListOf<Pair<LocalTime, LocalTime>>()

    var currentStart = sorted[0].startTime
    var currentEnd = sorted[0].endTime

    for (i in 1 until sorted.size) {
        val course = sorted[i]
        if (!course.startTime.isAfter(currentEnd)) {
            if (course.endTime.isAfter(currentEnd)) {
                currentEnd = course.endTime
            }
        } else {
            merged.add(currentStart to currentEnd)
            currentStart = course.startTime
            currentEnd = course.endTime
        }
    }
    merged.add(currentStart to currentEnd)
    return merged
}

fun calculateTotalClassHours(classes: List<CourseClass>): String {
    val merged = mergedClassIntervals(classes)
    val totalMinutes = merged.sumOf { (start, end) -> Duration.between(start, end).toMinutes() }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "$hours hr $mins m"
        hours > 0 -> "$hours hrs"
        else -> "$mins mins"
    }
}

fun calculateVacantTimeBlocks(classes: List<CourseClass>): List<VacantTimeBlock> {
    val defaultStart = LocalTime.of(8, 0)
    val defaultEnd = LocalTime.of(17, 0)

    if (classes.isEmpty()) {
        val duration = Duration.between(defaultStart, defaultEnd).toMinutes()
        return listOf(VacantTimeBlock(defaultStart, defaultEnd, duration))
    }

    val merged = mergedClassIntervals(classes)
    val earliestStart = merged.first().first
    val latestEnd = merged.maxOf { it.second }

    val windowStart = if (earliestStart.isBefore(defaultStart)) earliestStart else defaultStart
    val windowEnd = if (latestEnd.isAfter(defaultEnd)) latestEnd else defaultEnd

    val vacantBlocks = mutableListOf<VacantTimeBlock>()
    var currentPointer = windowStart

    for ((cStart, cEnd) in merged) {
        if (cStart.isAfter(windowEnd)) break

        if (cStart.isAfter(currentPointer)) {
            val gapMinutes = Duration.between(currentPointer, cStart).toMinutes()
            if (gapMinutes > 0) {
                vacantBlocks.add(VacantTimeBlock(currentPointer, cStart, gapMinutes))
            }
        }

        if (cEnd.isAfter(currentPointer)) {
            currentPointer = cEnd
        }
    }

    if (currentPointer.isBefore(windowEnd)) {
        val gapMinutes = Duration.between(currentPointer, windowEnd).toMinutes()
        if (gapMinutes > 0) {
            vacantBlocks.add(VacantTimeBlock(currentPointer, windowEnd, gapMinutes))
        }
    }

    return vacantBlocks
}

fun calculateTotalFreeTime(vacantBlocks: List<VacantTimeBlock>): String {
    val totalMinutes = vacantBlocks.sumOf { it.durationMinutes }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "$hours hr $mins m"
        hours > 0 -> "$hours hrs"
        else -> "$mins mins"
    }
}

fun formatWeekRange(start: LocalDate, end: LocalDate): String {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.US)
    val fullMonthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.US)

    return if (start.year == end.year) {
        if (start.month == end.month) {
            "${start.format(fullMonthFormatter)} ${start.dayOfMonth} – ${end.dayOfMonth}"
        } else {
            "${start.format(monthFormatter)} ${start.dayOfMonth} – ${end.format(monthFormatter)} ${end.dayOfMonth}"
        }
    } else {
        "${start.format(monthFormatter)} ${start.dayOfMonth}, ${start.year} – ${end.format(monthFormatter)} ${end.dayOfMonth}, ${end.year}"
    }
}
