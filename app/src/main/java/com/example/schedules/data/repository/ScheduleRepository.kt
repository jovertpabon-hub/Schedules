package com.example.schedules.data.repository

import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleConstants
import java.time.LocalTime

object ScheduleRepository {
    private val realWeekData: List<DayScheduleData> by lazy {
        listOf(
            DayScheduleData(
                dayName = "Monday",
                classes = listOf(
                    createClass("GE6", "Art Appreciation", "08:00 AM", "10:00 AM", "IITONLINE", "TBA"),
                    createClass("GE7", "Science, Tech & Society", "01:00 PM", "03:00 PM", "IITBLENDED", "J. Sedeño"),
                    createClass("CSP2", "Numerical Methods", "03:00 PM", "05:00 PM", "IITRM11", "S. Carilo"),
                    createClass("NC1", "Networks & Communication", "05:00 PM", "06:00 PM", "IITRM6", "N. Pura")
                )
            ),
            DayScheduleData(
                dayName = "Tuesday",
                classes = listOf(
                    createClass("SDF1", "Object Oriented Programming", "08:00 AM", "09:00 AM", "IITRM7", "N. Pura"),
                    createClass("CC4", "Data Structures & Algorithm", "09:00 AM", "10:00 AM", "IITONLINE", "J. Sabaria"),
                    createClass("GE7", "Science, Tech & Society", "02:00 PM", "03:00 PM", "IITBLENDED", "J. Sedeño"),
                    createClass("PATHFIT3", "Dance", "03:00 PM", "05:00 PM", "GYM6", " C. Sanoy")
                )
            ),
            DayScheduleData(
                dayName = "Wednesday",
                classes = listOf(
                    createClass("GE6", "Art Appreciation", "08:00 AM", "09:00 AM", "IITONLINE", "TBA"),
                    createClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM12", "N. Pura"),
                    createClass("CC4", "Data Structures & Algorithm", "01:00 PM", "03:00 PM", "IITONLINE", "J. Sabaria"),
                    createClass("CC4", "Data Structures & Algorithm", "03:00 PM", "05:00 PM", "IITRM12", "J. Sabaria")
                )
            ),
            DayScheduleData(
                dayName = "Thursday",
                classes = listOf(
                    createClass("GEE3-C", "Living in the IT Era", "08:00 AM", "09:00 AM", "IITRM20", "F. Oliver"),
                    createClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM7", "N. Pura"),
                    createClass("NC1", "Networks & Communication", "01:00 PM", "03:00 PM", "IITRM14", "N. Pura"),
                    createClass("CSP2", "Numerical Methods", "03:00 PM", "04:00 PM", "IITRM5", "S. Carilo")
                )
            ),
            DayScheduleData(
                dayName = "Friday",
                classes = listOf(
                    createClass("NC1", "Networks & Communication", "08:00 AM", "09:00 AM", "IITRM6", "N. Pura"),
                    createClass("GEE3-C", "Living in the IT Era", "10:00 AM", "12:00 PM", "IITRM20", "F. Oliver"),
                    createClass("CSP2", "Numerical Methods", "01:00 PM", "03:00 PM", "IITRM7", "S. Carilo")
                )
            ),
            DayScheduleData(dayName = "Saturday", classes = emptyList()),
            DayScheduleData(dayName = "Sunday", classes = emptyList())
        )
    }

    fun getWeekData(): List<DayScheduleData> = realWeekData

    private fun createClass(
        code: String,
        name: String,
        start: String,
        end: String,
        room: String,
        instructor: String
    ): CourseClass {
        return CourseClass(
            courseCode = code,
            courseName = name,
            startTime = LocalTime.parse(start, ScheduleConstants.TimeFormatter),
            endTime = LocalTime.parse(end, ScheduleConstants.TimeFormatter),
            room = room,
            instructor = instructor
        )
    }
}
