package com.example.schedules.data.local

import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromString(value: String?): LocalTime? {
        return value?.let {
            LocalTime.parse(it, formatter)
        }
    }

    @TypeConverter
    fun toString(time: LocalTime?): String? {
        return time?.format(formatter)
    }
}
