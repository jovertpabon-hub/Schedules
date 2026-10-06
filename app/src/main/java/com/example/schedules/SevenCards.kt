package com.example.schedules

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleScreen
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.dialogs.AddOrEditClassDialog
import com.example.schedules.ui.dialogs.ClearAllConfirmationDialog
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import com.example.schedules.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp() {
    val context = LocalContext.current
    val repository = (context.applicationContext as SchedulesApplication).repository
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(repository, context)
    )

    // UI States
    var currentScreen by remember { mutableStateOf(ScheduleScreen.NOW) }
    var showDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<CourseClass?>(null) }
    var editingDay by remember { mutableStateOf("") }
    var visibleDay by remember { mutableStateOf(ScheduleConstants.DaysOfWeek[0]) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val isRefreshing by viewModel.isDataLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importPdf(it) }
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
        floatingActionButton = {
            MultiActionFab(
                isExpanded = isFabExpanded,
                onExpandClick = { isFabExpanded = !isFabExpanded },
                items = listOf(
                    FabItem(
                        icon = Icons.Default.DeleteSweep,
                        label = "Clear All",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        onClick = {
                            isFabExpanded = false
                            showClearAllDialog = true
                        }
                    ),
                    FabItem(
                        icon = Icons.Default.PictureAsPdf,
                        label = "Import PDF",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            isFabExpanded = false
                            pdfLauncher.launch("application/pdf")
                        }
                    ),
                    FabItem(
                        icon = Icons.Default.Add,
                        label = "Add Class",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            isFabExpanded = false
                            editingClass = null
                            editingDay = visibleDay
                            showDialog = true
                        }
                    )
                )
            )
        }
    ) { paddingValues ->
        MainScreenContent(
            paddingValues = paddingValues,
            currentScreen = currentScreen,
            onScreenChange = { currentScreen = it },
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

        if (showClearAllDialog) {
            ClearAllConfirmationDialog(
                onDismiss = { showClearAllDialog = false },
                onConfirm = {
                    viewModel.clearAllSchedules()
                    showClearAllDialog = false
                }
            )
        }
    }
}

data class FabItem(
    val icon: ImageVector,
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

@Composable
private fun MultiActionFab(
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    items: List<FabItem>
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = item.onClick,
                            containerColor = item.containerColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onExpandClick,
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(rotation)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close Menu" else "Open Menu"
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    currentScreen: ScheduleScreen,
    onScreenChange: (ScheduleScreen) -> Unit,
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
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == ScheduleScreen.GRID) {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it }))
                        .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it }))
                } else {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it }))
                        .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it }))
                }
            },
            label = "ScreenTransition",
            modifier = Modifier.weight(1f)
        ) { screen ->
            when (screen) {
                ScheduleScreen.NOW -> {
                    val currentStatus by viewModel.liveStatus.collectAsState()
                    val todayRemainingClasses by viewModel.remainingClassesToday.collectAsState()
                    NowTabScreen(
                        status = currentStatus,
                        todayRemainingClasses = todayRemainingClasses.items,
                        isLoading = isRefreshing,
                        onOpenCardsView = { onScreenChange(ScheduleScreen.GRID) }
                    )
                }
                ScheduleScreen.GRID -> {
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
                        onVisibleDayChanged = onVisibleDayChanged,
                        onBackToMain = { onScreenChange(ScheduleScreen.NOW) }
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
