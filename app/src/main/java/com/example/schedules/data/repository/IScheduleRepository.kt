package com.example.schedules.data.repository

import com.example.schedules.data.local.ScheduleEntity
import kotlinx.coroutines.flow.Flow

interface IScheduleRepository {
    fun getAllSchedules(): Flow<List<ScheduleEntity>>
    fun getSchedulesByDay(dayName: String): Flow<List<ScheduleEntity>>
    suspend fun insertSchedule(schedule: ScheduleEntity)
    suspend fun updateSchedule(schedule: ScheduleEntity)
    suspend fun deleteSchedule(schedule: ScheduleEntity)
    suspend fun validateAndInsert(schedule: ScheduleEntity): Result<Unit>
}
