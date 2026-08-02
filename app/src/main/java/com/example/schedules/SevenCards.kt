package com.example.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleTab
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.dialogs.AddOrEditClassDialog
import com.example.schedules.ui.viewmodel.CrudEvent
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp() {
    val context = LocalContext.current
    val repository = (context.applicationContext as SchedulesApplication).repository
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(repository)
    )

    var showDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<CourseClass?>(null) }
    var editingDay by remember { mutableStateOf("") }
    var visibleDay by remember { mutableStateOf("Monday") }
    
    val pagerState = rememberPagerState(pageCount = { ScheduleTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CrudEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
                is CrudEvent.TriggerErrorHaptic -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is CrudEvent.RequestDismissDialog -> {
                    showDialog = false
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                ScheduleTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentPage == tab.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingClass = null
                editingDay = visibleDay
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false,
                beyondViewportPageCount = 1,
            ) { page ->
                when (ScheduleTab.entries[page]) {
                    ScheduleTab.NOW -> {
                        val currentStatus by viewModel.currentStatus.collectAsState()
                        val todayRemainingClasses by viewModel.todayRemainingClasses.collectAsState()
                        NowTabScreen(
                            status = currentStatus,
                            todayRemainingClasses = todayRemainingClasses.items
                        )
                    }
                    ScheduleTab.GRID -> {
                        val weekData by viewModel.weekData.collectAsState()
                        CardsViewTab(
                            weekSchedule = weekData,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            onEdit = { course, day -> 
                                editingClass = course
                                editingDay = day
                                showDialog = true 
                            },
                            onDelete = { course ->
                                course.id?.let { id ->
                                    val entity = ScheduleEntity(
                                        id = id,
                                        dayName = visibleDay,
                                        courseCode = course.courseCode,
                                        courseName = course.courseName,
                                        startTime = course.startTime,
                                        endTime = course.endTime,
                                        room = course.room,
                                        instructor = course.instructor
                                    )
                                    viewModel.deleteSchedule(entity)
                                }
                            },
                            onVisibleDayChanged = { visibleDay = it }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddOrEditClassDialog(
                editingClass = editingClass,
                editingDay = editingDay,
                onDismiss = { showDialog = false },
                onConfirm = { id, day, code, name, start, end, room, instructor ->
                    viewModel.addOrUpdateSchedule(id, day, code, name, start, end, room, instructor)
                },
                viewModel = viewModel
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainScheduleApp()
}
