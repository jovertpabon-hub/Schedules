package com.example.schedules.data.local

import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object LocalTimeConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): LocalTime? {
        return value?.let {
            LocalTime.parse(it, formatter)
        }
    }

    @TypeConverter
    @JvmStatic
    fun toString(time: LocalTime?): String? {
        return time?.format(formatter)
    }
}

object LocalDateConverter {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    @JvmStatic
    fun fromLocalDateString(value: String?): java.time.LocalDate? {
        return value?.let {
            java.time.LocalDate.parse(it, dateFormatter)
        }
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDateString(date: java.time.LocalDate?): String? {
        return date?.format(dateFormatter)
    }
}
