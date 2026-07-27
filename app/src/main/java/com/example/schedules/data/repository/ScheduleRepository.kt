package com.example.schedules.data.repository

import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData

class ScheduleRepository {
    private val realWeekData = listOf(
        DayScheduleData(
            dayName = "Monday",
            classes = listOf(
                CourseClass("GE6", "Art Appreciation", "08:00 AM", "10:00 AM", "IITONLINE", "TBA"),
                CourseClass("GE7", "Science, Tech & Society", "01:00 PM", "03:00 PM", "IITBLENDED", "J. Sedeño"),
                CourseClass("CSP2", "Numerical Methods", "03:00 PM", "05:00 PM", "IITRM11", "S. Carilo"),
                CourseClass("NC1", "Networks & Communication", "05:00 PM", "06:00 PM", "IITRM6", "N. Pura")
            )
        ),
        DayScheduleData(
            dayName = "Tuesday",
            classes = listOf(
                CourseClass("SDF1", "Object Oriented Programming", "08:00 AM", "09:00 AM", "IITRM7", "N. Pura"),
                CourseClass("CC4", "Data Structures & Algorithm", "09:00 AM", "10:00 AM", "IITONLINE", "J. Sabaria"),
                CourseClass("GE7", "Science, Tech & Society", "02:00 PM", "03:00 PM", "IITBLENDED", "J. Sedeño"),
                CourseClass("PATHFIT3", "Dance", "03:00 PM", "05:00 PM", "GYM6", " C. Sanoy")
            )
        ),
        DayScheduleData(
            dayName = "Wednesday",
            classes = listOf(
                CourseClass("GE6", "Art Appreciation", "08:00 AM", "09:00 AM", "IITONLINE", "TBA"),
                CourseClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM12", "N. Pura"),
                CourseClass("CC4", "Data Structures & Algorithm", "01:00 PM", "03:00 PM", "IITONLINE", "J. Sabaria"),
                CourseClass("CC4", "Data Structures & Algorithm", "03:00 PM", "05:00 PM", "IITRM12", "J. Sabaria")
            )
        ),
        DayScheduleData(
            dayName = "Thursday",
            classes = listOf(
                CourseClass("GEE3-C", "Living in the IT Era", "08:00 AM", "09:00 AM", "IITRM20", "F. Oliver"),
                CourseClass("SDF1", "Object Oriented Programming", "10:00 AM", "12:00 PM", "IITRM7", "N. Pura"),
                CourseClass("NC1", "Networks & Communication", "01:00 PM", "03:00 PM", "IITRM14", "N. Pura"),
                CourseClass("CSP2", "Numerical Methods", "03:00 PM", "04:00 PM", "IITRM5", "S. Carilo")
            )
        ),
        DayScheduleData(
            dayName = "Friday",
            classes = listOf(
                CourseClass("NC1", "Networks & Communication", "08:00 AM", "09:00 AM", "IITRM6", "N. Pura"),
                CourseClass("GEE3-C", "Living in the IT Era", "10:00 AM", "12:00 PM", "IITRM20", "F. Oliver"),
                CourseClass("CSP2", "Numerical Methods", "01:00 PM", "03:00 PM", "IITRM7", "S. Carilo")
            )
        ),
        DayScheduleData(dayName = "Saturday", classes = emptyList()),
        DayScheduleData(dayName = "Sunday", classes = emptyList())
    )

    fun getWeekData(): List<DayScheduleData> = realWeekData
}
