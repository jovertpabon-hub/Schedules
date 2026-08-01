package com.example.schedules.data.repository

import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import java.time.LocalTime

object ScheduleRepository {

    private data class CourseInfo(val name: String, val instructor: String)

    private data class Session(
        val code: String,
        val start: LocalTime,
        val end: LocalTime,
        val room: String
    )

    private val COURSE_CATALOG = mapOf(
        "GE6" to CourseInfo("Art Appreciation", "New Faculty"),
        "GE7" to CourseInfo("Science, Tech & Society", "J. Sedeño"),
        "CSP2" to CourseInfo("Numerical Methods and Analysis", "S. Carilo"),
        "NC1" to CourseInfo("Networks & Communication", "N. Pura"),
        "SDF1" to CourseInfo("Object Oriented Programming", "N. Pura"),
        "CC4" to CourseInfo("Data Structures & Algorithm", "J. Sabaria"),
        "PATHFIT3" to CourseInfo("Dance", "C. Sanoy"),
        "GEE3-C" to CourseInfo("Living in the IT Era", "F. Oliver")
    )

    private val MON = listOf(
        Session("GE6", LocalTime.of(8, 0), LocalTime.of(11, 0), "IITONLINE"),
        Session("GE7", LocalTime.of(13, 0), LocalTime.of(15, 0), "IITBLENDED"),
        Session("CSP2", LocalTime.of(15, 0), LocalTime.of(17, 0), "IITRM11"),
        Session("NC1", LocalTime.of(17, 0), LocalTime.of(18, 0), "IITRM6")
    )
    
    private val TUE = listOf(
        Session("SDF1", LocalTime.of(8, 0), LocalTime.of(9, 0), "IITRM7"),
        Session("CC4", LocalTime.of(9, 0), LocalTime.of(10, 0), "IITONLINE"),
        Session("GE7", LocalTime.of(14, 0), LocalTime.of(15, 0), "IITBLENDED"),
        Session("PATHFIT3", LocalTime.of(15, 0), LocalTime.of(17, 0), "GYM6")
    )
    
    private val WED = listOf(
        Session("SDF1", LocalTime.of(10, 0), LocalTime.of(12, 0), "IITRM12"),
        Session("CC4", LocalTime.of(13, 0), LocalTime.of(15, 0), "IITONLINE"),
        Session("CC4", LocalTime.of(15, 0), LocalTime.of(17, 0), "IITRM12"),
        Session("NC1", LocalTime.of(17, 0), LocalTime.of(18, 0), "IITRM6")
    )
    
    private val THU = listOf(
        Session("GEE3-C", LocalTime.of(8, 0), LocalTime.of(9, 0), "IITRM20"),
        Session("SDF1", LocalTime.of(10, 0), LocalTime.of(12, 0), "IITRM7"),
        Session("NC1", LocalTime.of(13, 0), LocalTime.of(15, 0), "IITRM14"),
        Session("CSP2", LocalTime.of(15, 0), LocalTime.of(16, 0), "IITRM5")
    )
    
    private val FRI = listOf(
        Session("NC1", LocalTime.of(8, 0), LocalTime.of(9, 0), "IITRM6"),
        Session("GEE3-C", LocalTime.of(10, 0), LocalTime.of(12, 0), "IITRM20"),
        Session("CSP2", LocalTime.of(13, 0), LocalTime.of(15, 0), "IITRM17")
    )
    
    private val SAT = emptyList<Session>()
    private val SUN = emptyList<Session>()

    private val realWeekData: List<DayScheduleData> by lazy {
        listOf(
            assembleDay("Monday", MON),
            assembleDay("Tuesday", TUE),
            assembleDay("Wednesday", WED),
            assembleDay("Thursday", THU),
            assembleDay("Friday", FRI),
            assembleDay("Saturday", SAT),
            assembleDay("Sunday", SUN)
        )
    }

    private fun assembleDay(dayName: String, sessions: List<Session>): DayScheduleData {
        val classes = sessions.map { session ->
            val info = COURSE_CATALOG[session.code] ?: CourseInfo("Unknown Course", "TBA")
            
            CourseClass(
                courseCode = session.code,
                courseName = info.name,
                startTime = session.start,
                endTime = session.end,
                room = session.room,
                instructor = info.instructor
            )
        }
        return DayScheduleData(dayName, classes)
    }

    fun getWeekData(): List<DayScheduleData> = realWeekData
}
