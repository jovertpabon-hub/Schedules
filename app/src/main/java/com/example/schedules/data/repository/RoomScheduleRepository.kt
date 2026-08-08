package com.example.schedules.data.repository

import com.example.schedules.data.local.ScheduleDao
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.ScheduleConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of the Schedule Repository using Room Database.
 * This class is the "Brain" of our data layer—it handles logic and talks to the DAO.
 */
class RoomScheduleRepository(private val scheduleDao: ScheduleDao) : IScheduleRepository {

    // --- Data Retrieval ---

    override fun getAllSchedules(): Flow<List<ScheduleEntity>> = 
        scheduleDao.fetchAllSchedules()

    // --- Direct Database Actions ---

    override suspend fun saveScheduleDirectly(schedule: ScheduleEntity) = 
        scheduleDao.saveSchedule(schedule)

    override suspend fun deleteSchedule(schedule: ScheduleEntity) = 
        scheduleDao.removeSchedule(schedule)

    // --- Complex Logic & Validation ---

    /**
     * Checks if a schedule is valid before saving it to the database.
     * This prevents users from adding classes that clash or have invalid times.
     */
    override suspend fun validateAndSaveSchedule(schedule: ScheduleEntity): Result<Unit> {
        
        // Step 1: Check if the times make sense
        // Logic: A class cannot end before it starts!
        if (schedule.endTime.isBefore(schedule.startTime) || schedule.endTime == schedule.startTime) {
            return Result.failure(Exception("End time must be after start time"))
        }

        // Step 2: Get all classes already scheduled for this specific day
        // We use .first() because we just need a snapshot of the current list once.
        val existingClasses = scheduleDao.fetchSchedulesForDay(schedule.dayName).first()

        // Step 3: Check for Time Overlaps
        // A conflict occurs if (NewStart < ExistingEnd) AND (NewEnd > ExistingStart)
        val hasOverlap = existingClasses.any { existing ->
            // If we are editing a class, don't compare it against its old self (check IDs)
            val isSameClass = schedule.id != ScheduleConstants.DEFAULT_ID && schedule.id == existing.id
            if (isSameClass) return@any false
            
            val overlaps = schedule.startTime.isBefore(existing.endTime) && 
                          schedule.endTime.isAfter(existing.startTime)
            overlaps
        }

        if (hasOverlap) {
            return Result.failure(Exception("This class overlaps with another one on ${schedule.dayName}"))
        }

        // Step 4: Check for Duplicate Course Codes
        // We generally don't want the same course code twice on the same day.
        val isDuplicateCode = existingClasses.any { existing ->
            val isSameClass = schedule.id != ScheduleConstants.DEFAULT_ID && schedule.id == existing.id
            if (isSameClass) return@any false
            
            existing.courseCode == schedule.courseCode 
        }
        
        if (isDuplicateCode) {
            return Result.failure(Exception("Course ${schedule.courseCode} is already scheduled for this day"))
        }

        // Step 5: Save to Database
        return try {
            scheduleDao.saveSchedule(schedule)
            Result.success(Unit) // Return success
        } catch (error: Exception) {
            Result.failure(error) // Return failure if the database had a problem
        }
    }
}
