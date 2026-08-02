package com.example.schedules.data.repository

import com.example.schedules.data.local.ScheduleDao
import com.example.schedules.data.local.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RoomScheduleRepository(private val scheduleDao: ScheduleDao) : IScheduleRepository {

    override fun getAllSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()

    override fun getSchedulesByDay(dayName: String): Flow<List<ScheduleEntity>> = 
        scheduleDao.getSchedulesByDay(dayName)

    override suspend fun insertSchedule(schedule: ScheduleEntity) = 
        scheduleDao.insertSchedule(schedule)

    override suspend fun updateSchedule(schedule: ScheduleEntity) = 
        scheduleDao.updateSchedule(schedule)

    override suspend fun deleteSchedule(schedule: ScheduleEntity) = 
        scheduleDao.deleteSchedule(schedule)

    override suspend fun validateAndInsert(schedule: ScheduleEntity): Result<Unit> {
        // 1. Basic Validation: End time after start time
        if (schedule.endTime.isBefore(schedule.startTime) || schedule.endTime == schedule.startTime) {
            return Result.failure(Exception("End time must be after start time"))
        }

        // 2. Overlap Validation
        val existingSchedules = scheduleDao.getSchedulesByDay(schedule.dayName).first()
        val hasOverlap = existingSchedules.any { existing ->
            // (StartA < EndB) and (EndA > StartB)
            // But ignore itself if we are updating (id check)
            if (schedule.id != 0L && schedule.id == existing.id) return@any false
            
            schedule.startTime.isBefore(existing.endTime) && schedule.endTime.isAfter(existing.startTime)
        }

        if (hasOverlap) {
            return Result.failure(Exception("Schedule overlaps with an existing class on ${schedule.dayName}"))
        }

        // 3. Duplicate Course Code on same day
        val isDuplicate = existingSchedules.any { 
            if (schedule.id != 0L && schedule.id == it.id) return@any false
            it.courseCode == schedule.courseCode 
        }
        if (isDuplicate) {
            return Result.failure(Exception("Course ${schedule.courseCode} already scheduled for ${schedule.dayName}"))
        }

        return try {
            // Use insertSchedule for both Add and Update since it has OnConflictStrategy.REPLACE
            // and handles restoring deleted items correctly.
            scheduleDao.insertSchedule(schedule)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
