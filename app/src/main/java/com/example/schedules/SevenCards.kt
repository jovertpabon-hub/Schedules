package com.example.schedules

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleTab
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.dialogs.AddOrEditClassDialog
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import com.example.schedules.ui.viewmodel.UiEvent
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

    // UI States
    var showDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<CourseClass?>(null) }
    var editingDay by remember { mutableStateOf("") }
    var visibleDay by remember { mutableStateOf(ScheduleConstants.DaysOfWeek[0]) }

    val pagerState = rememberPagerState(pageCount = { ScheduleTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing by viewModel.isDataLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importPdf(context, it) }
    }

    // Observe UI Events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.text,
                        actionLabel = event.actionText,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onActionClick?.invoke()
                    }
                }
                is UiEvent.VibrateError -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is UiEvent.CloseDialog -> {
                    showDialog = false
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ScheduleBottomBar(
                pagerState = pagerState,
                onTabSelected = { tab ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { pdfLauncher.launch("application/pdf") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Import PDF")
                }
                AddClassFab(
                    onClick = {
                        editingClass = null
                        editingDay = visibleDay
                        showDialog = true
                    }
                )
            }
        }
    ) { paddingValues ->
        MainPagerContent(
            paddingValues = paddingValues,
            pagerState = pagerState,
            viewModel = viewModel,
            isRefreshing = isRefreshing,
            visibleDay = visibleDay,
            onEditClass = { course, day ->
                editingClass = course
                editingDay = day
                showDialog = true
            },
            onVisibleDayChanged = { visibleDay = it }
        )

        if (showDialog) {
            AddOrEditClassDialog(
                editingClass = editingClass,
                editingDay = editingDay,
                onDismiss = { showDialog = false },
                onConfirm = { id, day, code, name, start, end, room, instructor ->
                    viewModel.addNewOrEditSchedule(id, day, code, name, start, end, room, instructor)
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ScheduleBottomBar(
    pagerState: PagerState,
    onTabSelected: (ScheduleTab) -> Unit
) {
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        ScheduleTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentPage == tab.ordinal,
                onClick = { onTabSelected(tab) },
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
}

@Composable
private fun AddClassFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Add Class")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MainPagerContent(
    paddingValues: PaddingValues,
    pagerState: PagerState,
    viewModel: ScheduleViewModel,
    isRefreshing: Boolean,
    visibleDay: String,
    onEditClass: (CourseClass, String) -> Unit,
    onVisibleDayChanged: (String) -> Unit
) {
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
                    val currentStatus by viewModel.liveStatus.collectAsState()
                    val todayRemainingClasses by viewModel.remainingClassesToday.collectAsState()
                    NowTabScreen(
                        status = currentStatus,
                        todayRemainingClasses = todayRemainingClasses.items,
                        isLoading = isRefreshing
                    )
                }
                ScheduleTab.GRID -> {
                    val weekData by viewModel.weekSchedule.collectAsState()
                    CardsViewTab(
                        weekSchedule = weekData,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshData() },
                        onEdit = onEditClass,
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
                                viewModel.deleteClass(entity)
                            }
                        },
                        onVisibleDayChanged = onVisibleDayChanged
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainScheduleApp()
}
