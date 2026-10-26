package com.example.schedules.data.repository

import android.content.Context
import com.example.schedules.data.local.ScheduleDao
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.notifications.NotificationPreferences
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of the Schedule Repository using Room Database.
 * This class is the "Brain" of our data layer—it handles logic and talks to the DAO.
 */
class RoomScheduleRepository(
    private val scheduleDao: ScheduleDao,
    context: Context
) : IScheduleRepository {

    private val appContext = context.applicationContext

    // --- Data Retrieval ---

    override fun getAllSchedules(): Flow<List<ScheduleEntity>> = 
        scheduleDao.fetchAllSchedules()

    // --- Direct Database Actions ---

    override suspend fun saveScheduleDirectly(schedule: ScheduleEntity) {
        val savedId = scheduleDao.saveSchedule(schedule)
        val entityWithId = if (schedule.id == ScheduleConstants.DEFAULT_ID) schedule.copy(id = savedId) else schedule
        registerReminderForSchedule(entityWithId)
    }

    override suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.removeSchedule(schedule)
        ScheduleNotificationHelper.cancelClassReminder(appContext, schedule)
    }

    override suspend fun clearAllSchedules() {
        val allSchedules = scheduleDao.fetchAllSchedulesDirect()
        allSchedules.forEach { schedule ->
            ScheduleNotificationHelper.cancelClassReminder(appContext, schedule)
        }
        scheduleDao.clearAll()
    }

    // --- Complex Logic & Validation ---

    /**
     * Checks if a schedule is valid before saving it to the database.
     * This prevents users from adding classes that clash or have invalid times.
     */
    override suspend fun validateAndSaveSchedule(schedule: ScheduleEntity): Result<Unit> {
        
        // Step 1: Check if the times make sense
        if (schedule.endTime.isBefore(schedule.startTime) || schedule.endTime == schedule.startTime) {
            return Result.failure(Exception("End time must be after start time"))
        }

        // Step 2: Get all classes already scheduled for this specific day using direct query
        val existingClasses = scheduleDao.getSchedulesForDayDirect(schedule.dayName)

        // Step 3: Check for Time Overlaps
        val hasOverlap = existingClasses.any { existing ->
            val isSameClass = schedule.id != ScheduleConstants.DEFAULT_ID && schedule.id == existing.id
            if (isSameClass) return@any false
            
            val overlaps = schedule.startTime.isBefore(existing.endTime) && 
                          schedule.endTime.isAfter(existing.startTime)
            overlaps
        }

        if (hasOverlap) {
            return Result.failure(Exception("This class overlaps with another one on ${schedule.dayName}"))
        }

        // Step 4: Save to Database & Propagate instructor update across same course code sessions
        return try {
            val savedId = scheduleDao.saveSchedule(schedule)
            val entityWithId = if (schedule.id == ScheduleConstants.DEFAULT_ID) schedule.copy(id = savedId) else schedule
            registerReminderForSchedule(entityWithId)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun registerReminderForSchedule(schedule: ScheduleEntity) {
        val isEnabled = NotificationPreferences.isNotificationsEnabled(appContext)
        if (!isEnabled) return

        val offsetMinutes = NotificationPreferences.getReminderOffsetMinutes(appContext)
        val adjustedStartTime = schedule.startTime.minusMinutes(offsetMinutes.toLong())

        // Schedule exact alarm for future occurrences via AlarmManager
        ScheduleNotificationHelper.scheduleExactClassReminder(
            appContext,
            schedule,
            adjustedStartTime,
            offsetMinutes
        )
    }
}
