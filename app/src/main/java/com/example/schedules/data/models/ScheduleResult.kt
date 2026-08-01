package com.example.schedules.data.models

sealed class ScheduleResult<out T> {
    data class Success<out T>(val data: T) : ScheduleResult<T>()
    data class Error(val message: String) : ScheduleResult<Nothing>()
    object Loading : ScheduleResult<Nothing>()
}

sealed class ValidationError {
    object EmptyFields : ValidationError()
    object InvalidTimeFormat : ValidationError()
    object EndBeforeStart : ValidationError()
    object OverlappingSchedule : ValidationError()
    object DuplicateCourseCode : ValidationError()
}
