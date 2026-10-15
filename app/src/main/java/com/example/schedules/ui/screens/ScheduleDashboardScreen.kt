package com.example.schedules.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.DayScheduleData
import com.example.schedules.data.models.ScheduleStatus
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.components.SkeletonDashboardLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * Represents the UI state lifecycle for the Schedule Dashboard.
 */
sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val status: ScheduleStatus,
        val weekSchedule: WeekSchedule,
        val selectedDay: String
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

/**
 * ViewModel managing async startup data fetching via Coroutines to prevent UI lag.
 */
class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                // Simulate asynchronous background data fetching (PDF parsing / Room DB load)
                delay(1200)

                val sampleClasses = listOf(
                    CourseClass(
                        id = 1L,
                        courseCode = "CS 101",
                        courseName = "Introduction to Computer Science",
                        startTime = LocalTime.of(8, 0),
                        endTime = LocalTime.of(10, 0),
                        room = "Room 301",
                        instructor = "Dr. Alan Turing"
                    ),
                    CourseClass(
                        id = 2L,
                        courseCode = "MATH 201",
                        courseName = "Linear Algebra & Calculus",
                        startTime = LocalTime.of(10, 30),
                        endTime = LocalTime.of(12, 30),
                        room = "Lab 102",
                        instructor = "Prof. Ada Lovelace"
                    )
                )

                val daySchedule = DayScheduleData("Monday", sampleClasses)
                val weekSchedule = WeekSchedule(listOf(daySchedule))
                val status = ScheduleStatus(
                    courseClass = sampleClasses.first(),
                    dayName = "Monday",
                    status = ClassStatus.ONGOING,
                    remainingFormatted = "00:45:12",
                    progressPercent = 0.62f
                )

                _uiState.value = DashboardUiState.Success(
                    status = status,
                    weekSchedule = weekSchedule,
                    selectedDay = "Monday"
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    message = e.localizedMessage ?: "An unexpected error occurred while loading schedule data."
                )
            }
        }
    }
}

/**
 * Jetpack Compose Screen that displays a Skeleton Loading layout on startup
 * and smoothly transitions to actual content using Crossfade.
 */
@Composable
fun ScheduleDashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Crossfade(
        targetState = uiState,
        animationSpec = tween(durationMillis = 500),
        label = "DashboardStateTransition",
        modifier = modifier.fillMaxSize()
    ) { state ->
        when (state) {
            is DashboardUiState.Loading -> {
                // Immediately rendered on launch to eliminate layout shift and perceived lag
                SkeletonDashboardLayout()
            }
            is DashboardUiState.Success -> {
                // Smooth transition once asynchronous data loading completes
                NowTabScreen(
                    status = state.status,
                    weekSchedule = state.weekSchedule,
                    selectedDay = state.selectedDay,
                    onSelectedDayChanged = { /* Handle day change */ }
                )
            }
            is DashboardUiState.Error -> {
                // Error state with retry affordance
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchDashboardData() }) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}