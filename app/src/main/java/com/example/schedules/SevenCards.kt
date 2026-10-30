package com.example.schedules

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.components.SkeletonDashboardLayout
import com.example.schedules.ui.dialogs.AddOrEditClassDialog
import com.example.schedules.ui.dialogs.ClearAllConfirmationDialog
import com.example.schedules.ui.dialogs.NotificationSettingsDialog
import com.example.schedules.ui.dialogs.ScheduleImportPreviewDialog
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import com.example.schedules.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = (appContext as SchedulesApplication).repository
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(repository, appContext)
    )

    val importPreview by viewModel.importPreview.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = java.time.LocalDate.now()
            kotlinx.coroutines.delay(1.minutes.inWholeMilliseconds.milliseconds)
        }
    }

    val todayName = remember(currentDate) {
        val name = currentDate.dayOfWeek.name
        ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(name, ignoreCase = true) } ?: ScheduleConstants.DaysOfWeek[0]
    }

    var selectedDay by remember { mutableStateOf(todayName) }
    var showDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<CourseClass?>(null) }
    var editingDay by remember { mutableStateOf("") }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importPdf(it) }
    }

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
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                MultiActionFab(
                    isExpanded = isFabExpanded,
                    onExpandClick = { isFabExpanded = !isFabExpanded },
                    items = listOf(
                        FabItem(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = {
                                isFabExpanded = false
                                showNotificationSettings = true
                            }
                        ),
                        FabItem(
                            icon = Icons.Default.DeleteSweep,
                            label = "Clear All",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = {
                                isFabExpanded = false
                                showClearAllDialog = true
                            }
                        ),
                        FabItem(
                            icon = Icons.Default.PictureAsPdf,
                            label = "Import COE",
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
                                editingDay = selectedDay
                                showDialog = true
                            }
                        )
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            MainScreenContent(
                paddingValues = paddingValues,
                viewModel = viewModel,
                selectedDay = selectedDay,
                onSelectedDayChanged = { selectedDay = it },
                onEditClass = { course, day ->
                    editingClass = course
                    editingDay = day
                    showDialog = true
                }
            )

            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isFabExpanded = false
                        }
                )
            }
        }

        if (showDialog) {
            AddOrEditClassDialog(
                editingClass = editingClass,
                editingDay = editingDay,
                onDismiss = { showDialog = false },
                onConfirm = { id, day, code, name, start, end, room ->
                    viewModel.addNewOrEditSchedule(id, day, code, name, start, end, room)
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

        if (showNotificationSettings) {
            NotificationSettingsDialog(
                onDismiss = { showNotificationSettings = false }
            )
        }

        importPreview?.let { preview ->
            ScheduleImportPreviewDialog(
                preview = preview,
                onConfirm = { viewModel.confirmImport() },
                onCancel = { viewModel.cancelImport() }
            )
        }
    }
}

data class FabItem(
    val icon: ImageVector,
    val label: String,
    val containerColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun MultiActionFab(
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    items: List<FabItem>
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 16.dp,
        animationSpec = tween(durationMillis = 120),
        label = "FabCornerRadius"
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = item.onClick,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            containerColor = item.containerColor,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onExpandClick,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
            contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
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
    viewModel: ScheduleViewModel,
    selectedDay: String,
    onSelectedDayChanged: (String) -> Unit,
    onEditClass: (CourseClass, String) -> Unit
) {
    val currentStatus by viewModel.liveStatus.collectAsState()
    val weekSchedule by viewModel.weekSchedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Crossfade(
        targetState = isLoading,
        animationSpec = tween(durationMillis = 500),
        label = "DashboardLoadingTransition",
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) { loading ->
        if (loading) {
            SkeletonDashboardLayout()
        } else {
            NowTabScreen(
                status = currentStatus,
                weekSchedule = weekSchedule,
                selectedDay = selectedDay,
                onSelectedDayChanged = onSelectedDayChanged,
                onEditClass = onEditClass,
                onDeleteClass = { course ->
                    course.id?.let { id ->
                        val entity = ScheduleEntity(
                            id = id,
                            dayName = selectedDay,
                            courseCode = course.courseCode,
                            courseName = course.courseName,
                            startTime = course.startTime,
                            endTime = course.endTime,
                            room = course.room
                        )
                        viewModel.deleteClass(entity)
                    }
                }
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
