package com.example.schedules.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the Schedule database.
 * This interface defines all the possible ways we can interact with our "schedules" table.
 */
@Dao
interface ScheduleDao {

    /**
     * Retrieves all schedule entries from the database.
     * We use [Flow] so the UI can automatically update whenever the data changes.
     * The results are ordered first by the day of the week, then by the start time.
     */
    @Query("SELECT * FROM schedules ORDER BY dayName, startTime")
    fun fetchAllSchedules(): Flow<List<ScheduleEntity>>

    /**
     * One-shot fetch for all schedules without Flow observation.
     */
    @Query("SELECT * FROM schedules")
    suspend fun fetchAllSchedulesDirect(): List<ScheduleEntity>

    /**
     * Filters and retrieves schedules for a specific day (e.g., "Monday").
     * The :dayName syntax tells Room to use the function's parameter in the SQL query.
     */
    @Query("SELECT * FROM schedules WHERE dayName = :dayName ORDER BY startTime")
    fun fetchSchedulesForDay(dayName: String): Flow<List<ScheduleEntity>>

    /**
     * One-shot fetch for a specific day without Flow observation overhead.
     */
    @Query("SELECT * FROM schedules WHERE dayName = :dayName ORDER BY startTime")
    suspend fun getSchedulesForDayDirect(dayName: String): List<ScheduleEntity>

    /**
     * One-shot fetch by primary key ID.
     */
    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    /**
     * Adds a new schedule entry to the database and returns its row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchedule(newSchedule: ScheduleEntity): Long

    /**
     * Updates an existing schedule entry in the database.
     * Room uses the Primary Key (the ID) to find which row to update.
     */
    @Update
    suspend fun updateExistingSchedule(updatedSchedule: ScheduleEntity)

    /**
     * Removes a specific schedule entry from the database.
     */
    @Delete
    suspend fun removeSchedule(scheduleToDelete: ScheduleEntity)

    /**
     * Removes all schedule entries from the database.
     */
    @Query("DELETE FROM schedules")
    suspend fun clearAll()
}
