package com.example.schedules.data.notifications

import com.example.schedules.data.models.CourseClass
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FormattedNotificationContent(
    val title: String,
    val contentText: String,
    val expandedTitle: String,
    val expandedText: String,
    val publicTitle: String,
    val publicContentText: String
)

object NotificationContentFormatter {

    private val DisplayTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val CompactTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.US)

    fun formatUpcomingReminder(
        course: CourseClass,
        offsetMinutes: Int,
        showDetails: Boolean
    ): FormattedNotificationContent {
        val publicTitle = "Upcoming class"
        val publicContentText = "You have a class starting soon."

        if (!showDetails) {
            return FormattedNotificationContent(
                title = publicTitle,
                contentText = publicContentText,
                expandedTitle = publicTitle,
                expandedText = publicContentText,
                publicTitle = publicTitle,
                publicContentText = publicContentText
            )
        }

        val collapsedCountdown = formatCountdownShort(offsetMinutes)
        val expandedCountdown = formatCountdownLong(offsetMinutes)

        val title = "Class in $collapsedCountdown · ${course.courseCode}"
        val expandedTitle = "Class in $expandedCountdown"

        val formattedTime = course.startTime.format(DisplayTimeFormatter)

        val contentParts = mutableListOf<String>()
        if (course.courseName.isNotBlank() && !course.courseName.equals(course.courseCode, ignoreCase = true)) {
            contentParts.add(course.courseName)
        }
        contentParts.add(formattedTime)
        if (course.room.isNotBlank() && !course.room.equals("TBA", ignoreCase = true)) {
            contentParts.add(course.room)
        } else if (course.room.equals("TBA", ignoreCase = true)) {
            contentParts.add("Room TBA")
        }

        val contentText = contentParts.joinToString(" · ")

        val expandedTextBuilder = StringBuilder()
        if (course.courseName.isNotBlank() && !course.courseName.equals(course.courseCode, ignoreCase = true)) {
            expandedTextBuilder.append("${course.courseCode} — ${course.courseName}\n")
        } else {
            expandedTextBuilder.append("${course.courseCode}\n")
        }
        expandedTextBuilder.append("Time: $formattedTime\n")
        if (course.room.isNotBlank()) {
            expandedTextBuilder.append("Location: ${course.room}")
        } else {
            expandedTextBuilder.append("Location: TBA")
        }

        return FormattedNotificationContent(
            title = title,
            contentText = contentText,
            expandedTitle = expandedTitle,
            expandedText = expandedTextBuilder.toString().trimEnd(),
            publicTitle = publicTitle,
            publicContentText = publicContentText
        )
    }

    fun formatClassStarting(
        course: CourseClass,
        showDetails: Boolean
    ): FormattedNotificationContent {
        val publicTitle = "Class starting"
        val publicContentText = "Your scheduled class is starting now."

        if (!showDetails) {
            return FormattedNotificationContent(
                title = publicTitle,
                contentText = publicContentText,
                expandedTitle = publicTitle,
                expandedText = publicContentText,
                publicTitle = publicTitle,
                publicContentText = publicContentText
            )
        }

        val title = "Class starting · ${course.courseCode}"
        val expandedTitle = "Class starting now"

        val formattedTime = course.startTime.format(DisplayTimeFormatter)

        val contentParts = mutableListOf<String>()
        if (course.courseName.isNotBlank() && !course.courseName.equals(course.courseCode, ignoreCase = true)) {
            contentParts.add(course.courseName)
        }
        if (course.room.isNotBlank() && !course.room.equals("TBA", ignoreCase = true)) {
            contentParts.add(course.room)
        } else if (course.room.equals("TBA", ignoreCase = true)) {
            contentParts.add("Room TBA")
        }

        val contentText = if (contentParts.isEmpty()) formattedTime else contentParts.joinToString(" · ")

        val expandedTextBuilder = StringBuilder()
        if (course.courseName.isNotBlank() && !course.courseName.equals(course.courseCode, ignoreCase = true)) {
            expandedTextBuilder.append("${course.courseCode} — ${course.courseName}\n")
        } else {
            expandedTextBuilder.append("${course.courseCode}\n")
        }
        expandedTextBuilder.append("Scheduled time: $formattedTime\n")
        if (course.room.isNotBlank()) {
            expandedTextBuilder.append("Location: ${course.room}")
        } else {
            expandedTextBuilder.append("Location: TBA")
        }

        return FormattedNotificationContent(
            title = title,
            contentText = contentText,
            expandedTitle = expandedTitle,
            expandedText = expandedTextBuilder.toString().trimEnd(),
            publicTitle = publicTitle,
            publicContentText = publicContentText
        )
    }

    fun formatDailySummary(
        todayClasses: List<CourseClass>,
        showDetails: Boolean
    ): FormattedNotificationContent {
        val publicTitle = "Daily schedule summary"
        val count = todayClasses.size

        if (todayClasses.isEmpty()) {
            val noClassTitle = "No classes today"
            val noClassMsg = "Your schedule is clear. Enjoy your free day!"
            return FormattedNotificationContent(
                title = noClassTitle,
                contentText = noClassMsg,
                expandedTitle = noClassTitle,
                expandedText = noClassMsg,
                publicTitle = publicTitle,
                publicContentText = "No classes scheduled for today."
            )
        }

        val publicContentText = if (count == 1) "1 class scheduled today." else "$count classes scheduled today."

        if (!showDetails) {
            return FormattedNotificationContent(
                title = publicTitle,
                contentText = publicContentText,
                expandedTitle = publicTitle,
                expandedText = publicContentText,
                publicTitle = publicTitle,
                publicContentText = publicContentText
            )
        }

        val sortedClasses = todayClasses.sortedBy { it.startTime }

        val title = if (count == 1) "1 class today" else "$count classes today"
        val expandedTitle = "Your schedule today"

        val previewLimit = 2
        val previewClasses = sortedClasses.take(previewLimit)
        val remainingCount = count - previewLimit

        val previewParts = previewClasses.map { cls ->
            val timeStr = cls.startTime.format(CompactTimeFormatter)
            "$timeStr ${cls.courseCode}"
        }.toMutableList()

        if (remainingCount > 0) {
            previewParts.add("+$remainingCount")
        }

        val contentText = previewParts.joinToString(" · ")

        val expandedTextBuilder = StringBuilder()
        val countHeader = if (count == 1) "1 class scheduled" else "$count classes scheduled"
        expandedTextBuilder.append("$countHeader\n")

        sortedClasses.forEach { cls ->
            val timeStr = cls.startTime.format(DisplayTimeFormatter)
            expandedTextBuilder.append("• $timeStr — ${cls.courseCode}\n")
        }

        return FormattedNotificationContent(
            title = title,
            contentText = contentText,
            expandedTitle = expandedTitle,
            expandedText = expandedTextBuilder.toString().trimEnd(),
            publicTitle = publicTitle,
            publicContentText = publicContentText
        )
    }

    fun formatScheduleChange(
        courseCode: String?,
        changeDescription: String
    ): FormattedNotificationContent {
        val title = if (!courseCode.isNullOrBlank()) {
            "Schedule updated · $courseCode"
        } else {
            "Schedule updated"
        }
        val publicTitle = "Schedule update"
        val publicContentText = "Schedule information updated."

        return FormattedNotificationContent(
            title = title,
            contentText = changeDescription,
            expandedTitle = title,
            expandedText = changeDescription,
            publicTitle = publicTitle,
            publicContentText = publicContentText
        )
    }

    private fun formatCountdownShort(offsetMinutes: Int): String {
        return when {
            offsetMinutes <= 0 -> "soon"
            offsetMinutes % 60 == 0 -> "${offsetMinutes / 60} hr"
            else -> "$offsetMinutes min"
        }
    }

    private fun formatCountdownLong(offsetMinutes: Int): String {
        return when {
            offsetMinutes <= 0 -> "soon"
            offsetMinutes == 60 -> "1 hour"
            offsetMinutes > 60 && offsetMinutes % 60 == 0 -> "${offsetMinutes / 60} hours"
            offsetMinutes == 1 -> "1 minute"
            else -> "$offsetMinutes minutes"
        }
    }
}
