package com.example.schedules

import android.app.Application
import com.example.schedules.data.local.ScheduleDatabase
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import com.example.schedules.data.repository.RoomScheduleRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class SchedulesApplication : Application() {
    private val database by lazy { ScheduleDatabase.getDatabase(this) }
    val repository by lazy { RoomScheduleRepository(database.scheduleDao(), this) }

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        ScheduleNotificationHelper.createNotificationChannel(this)
    }
}
