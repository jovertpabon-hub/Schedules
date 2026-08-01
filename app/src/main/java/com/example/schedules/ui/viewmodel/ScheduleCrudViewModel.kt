package com.example.schedules.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.repository.IScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalTime

sealed class ScheduleUiState {
    object Idle : ScheduleUiState()
    object Loading : ScheduleUiState()
    data class Success(val schedules: List<ScheduleEntity>) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

class ScheduleCrudViewModel(private val repository: IScheduleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAllSchedules()
    }

    private fun loadAllSchedules() {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            repository.getAllSchedules()
                .catch { e ->
                    _uiState.value = ScheduleUiState.Error(e.message ?: "Unknown error")
                }
                .collect { schedules ->
                    _uiState.value = ScheduleUiState.Success(schedules)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000) // Simulate sync delay
            _isRefreshing.value = false
        }
    }

    fun addOrUpdateSchedule(
        id: Long? = null,
        day: String,
        code: String,
        name: String,
        startTime: LocalTime,
        endTime: LocalTime,
        room: String,
        instructor: String
    ) {
        viewModelScope.launch {
            _validationError.value = null
            
            if (day.isBlank() || code.isBlank() || name.isBlank() || room.isBlank() || instructor.isBlank()) {
                _validationError.value = "All fields must be filled"
                return@launch
            }

            val entity = ScheduleEntity(
                id = id ?: 0,
                dayName = day,
                courseCode = code,
                courseName = name,
                startTime = startTime,
                endTime = endTime,
                room = room,
                instructor = instructor
            )

            repository.validateAndInsert(entity).onFailure { error ->
                _validationError.value = error.message
            }.onSuccess {
                _validationError.value = null
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            val dummy = ScheduleEntity(id = id, dayName = "", courseCode = "", courseName = "", startTime = LocalTime.now(), endTime = LocalTime.now(), room = "", instructor = "")
            repository.deleteSchedule(dummy)
        }
    }

    class Factory(private val repository: IScheduleRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleCrudViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ScheduleCrudViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
