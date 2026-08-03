package com.example.schedules

import android.app.Application
import com.example.schedules.data.local.ScheduleDatabase
import com.example.schedules.data.repository.RoomScheduleRepository

class SchedulesApplication : Application() {
    private val database by lazy { ScheduleDatabase.getDatabase(this) }
    val repository by lazy { RoomScheduleRepository(database.scheduleDao()) }
}
