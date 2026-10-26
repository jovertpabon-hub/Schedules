package com.example.schedules.data.models

import androidx.compose.runtime.Immutable
import java.util.Locale

/**
 * Holds the extracted schedule data and calculated volume/size metrics
 * for the user confirmation step before saving to storage.
 */
@Immutable
data class ScheduleImportPreview(
    val schedules: List<ExtractedSchedule>,
    val totalEntries: Int,
    val payloadSizeBytes: Long,
    val payloadSizeFormatted: String,
    val characterCount: Int,
    val sourceName: String = "PDF Document"
) {
    companion object {
        fun create(
            schedules: List<ExtractedSchedule>,
            rawText: String,
            sourceName: String = "PDF Document"
        ): ScheduleImportPreview {
            val bytes = rawText.toByteArray(Charsets.UTF_8).size.toLong()
            val formattedSize = formatPayloadSize(bytes)
            return ScheduleImportPreview(
                schedules = schedules,
                totalEntries = schedules.size,
                payloadSizeBytes = bytes,
                payloadSizeFormatted = formattedSize,
                characterCount = rawText.length,
                sourceName = sourceName
            )
        }

        fun formatPayloadSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < (1024 * 1024) -> String.format(Locale.US, "%.2f KB", bytes / 1024.0)
                else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            }
        }
    }
}
