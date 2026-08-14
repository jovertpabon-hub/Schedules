package com.example.schedules.data.parser

import com.example.schedules.data.models.ExtractedSchedule

interface IScheduleParser {
    fun parse(text: String): List<ExtractedSchedule>
}
