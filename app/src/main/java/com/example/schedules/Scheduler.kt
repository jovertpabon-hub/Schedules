package com.example.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class IncomingClassResult(
    val courseClass: CourseClass?,
    val dayName: String,
    val isToday: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
fun getDynamicIncomingClass(weekData: List<DayScheduleData>): IncomingClassResult {
    val today = LocalDate.now().dayOfWeek.name // e.g. "MONDAY"
    val now = LocalTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    // Find today's schedule from weekData
    val todaySchedule = weekData.firstOrNull { it.dayName.equals(today, ignoreCase = true) }

    if (todaySchedule != null) {
        // Find the first class today that hasn't ended yet
        val upcomingToday = todaySchedule.classes.firstOrNull { course ->
            runCatching {
                val endTime = LocalTime.parse(course.endTime, timeFormatter)
                now.isBefore(endTime)
            }.getOrDefault(true)
        }

        if (upcomingToday != null) {
            return IncomingClassResult(upcomingToday, todaySchedule.dayName, isToday = true)
        }
    }

    // If today has no classes left, look for the next day with classes
    val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val todayIndex = dayOrder.indexOfFirst { it.equals(today, ignoreCase = true) }

    for (i in 1..6) {
        val nextDayIndex = (todayIndex + i) % 7
        val nextDayName = dayOrder[nextDayIndex]
        val nextDaySchedule = weekData.firstOrNull { it.dayName == nextDayName }

        if (nextDaySchedule != null && nextDaySchedule.classes.isNotEmpty()) {
            return IncomingClassResult(
                courseClass = nextDaySchedule.classes.first(),
                dayName = nextDaySchedule.dayName,
                isToday = false
            )
        }
    }

    return IncomingClassResult(null, "Today", isToday = true)
}