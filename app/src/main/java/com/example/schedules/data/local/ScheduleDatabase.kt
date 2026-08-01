package com.example.schedules.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.schedules.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScheduleEntity::class], version = 1, exportSchema = false)
@TypeConverters(LocalTimeConverter::class)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null

        fun getDatabase(context: Context): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "schedule_database"
                )
                .addCallback(ScheduleDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class ScheduleDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDatabase(database.scheduleDao())
                    }
                }
            }

            private suspend fun prepopulateDatabase(dao: ScheduleDao) {
                val staticData = ScheduleRepository.getWeekData()
                staticData.forEach { day ->
                    day.classes.forEach { course ->
                        dao.insertSchedule(
                            ScheduleEntity(
                                dayName = day.dayName,
                                courseCode = course.courseCode,
                                courseName = course.courseName,
                                startTime = course.startTime,
                                endTime = course.endTime,
                                room = course.room,
                                instructor = course.instructor
                            )
                        )
                    }
                }
            }
        }
    }
}
