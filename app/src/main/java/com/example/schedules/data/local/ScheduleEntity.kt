package com.example.schedules.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayName: String,
    val courseCode: String,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String,
    val instructor: String
)
