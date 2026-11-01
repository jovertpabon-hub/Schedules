package com.example.schedules.data.repository

import com.example.schedules.data.local.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * The Interface for our Schedule Repository.
 * Think of this as a "Rule Book" that defines what the Repository MUST be able to do.
 * Using an interface makes it easier to swap out the database logic later or write tests.
 */
interface IScheduleRepository {
    /** Gets a live stream of all schedules stored in the database */
    fun getAllSchedules(): Flow<List<ScheduleEntity>>
    
    /** Directly adds a schedule without extra validation (used for Undoing deletes) */
    suspend fun saveScheduleDirectly(schedule: ScheduleEntity)
    
    /** Removes a schedule from the database */
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    /** Removes all schedules from the database */
    suspend fun clearAllSchedules()
    
    /** 
     * The main way to add or edit a schedule.
     * It checks for errors (like overlapping times) before saving.
     * Returns a Result: Success if saved, or Failure with an error message.
     */
    suspend fun validateAndSaveSchedule(schedule: ScheduleEntity): Result<Unit>

    /** Gets live stream of all temporary notes */
    fun getAllNotes(): Flow<List<com.example.schedules.data.local.ScheduleNoteEntity>>

    /** Gets live stream of temporary notes for a specific date */
    fun getNotesForDate(date: java.time.LocalDate): Flow<List<com.example.schedules.data.local.ScheduleNoteEntity>>

    /** Saves or updates a temporary note for a class occurrence on a specific date */
    suspend fun saveNoteForScheduleAndDate(scheduleId: Long, date: java.time.LocalDate, noteText: String)

    /** Deletes a temporary note for a class occurrence on a specific date */
    suspend fun deleteNoteForScheduleAndDate(scheduleId: Long, date: java.time.LocalDate)
}
