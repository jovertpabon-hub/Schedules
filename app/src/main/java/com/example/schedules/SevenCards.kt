package com.example.schedules

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.local.ScheduleEntity
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.components.SkeletonDashboardLayout
import com.example.schedules.ui.dialogs.AboutSettingsDialog
import com.example.schedules.ui.dialogs.AddOrEditClassDialog
import com.example.schedules.ui.dialogs.AppearanceSettingsDialog
import com.example.schedules.ui.dialogs.ClearAllConfirmationDialog
import com.example.schedules.ui.dialogs.NotificationSettingsDialog
import com.example.schedules.ui.dialogs.ScheduleImportPreviewDialog
import com.example.schedules.ui.dialogs.ScheduleSettingsDialog
import com.example.schedules.ui.dialogs.SettingsOverviewDialog
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import com.example.schedules.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    intent: android.content.Intent? = null
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = (appContext as SchedulesApplication).repository
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(repository, appContext)
    )

    val importPreview by viewModel.importPreview.collectAsState()

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = LocalDate.now()
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
    var showSettingsOverview by remember { mutableStateOf(false) }
    var showScheduleSettings by remember { mutableStateOf(false) }
    var showAppearanceSettings by remember { mutableStateOf(false) }
    var showAboutSettings by remember { mutableStateOf(false) }

    LaunchedEffect(intent?.data) {
        val uri = intent?.data ?: return@LaunchedEffect
        if (uri.scheme == "coffeelog") {
            val host = uri.host
            if (host == "today") {
                selectedDay = todayName
            } else if (host == "class") {
                val classIdStr = uri.lastPathSegment
                val classId = classIdStr?.toLongOrNull()
                if (classId != null) {
                    val weekData = viewModel.weekSchedule.value
                    for (dayData in weekData.days) {
                        val found = dayData.classes.firstOrNull { it.id == classId }
                        if (found != null) {
                            selectedDay = dayData.dayName
                            editingClass = found
                            editingDay = dayData.dayName
                            showDialog = true
                            break
                        }
                    }
                }
            }
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                },
                onAddClass = {
                    editingClass = null
                    editingDay = selectedDay
                    showDialog = true
                },
                onImportPdf = {
                    pdfLauncher.launch("application/pdf")
                },
                onClearAll = {
                    showClearAllDialog = true
                },
                onSettingsClick = {
                    showSettingsOverview = true
                },
                onAboutClick = { showAboutSettings = true }
            )
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

        if (showSettingsOverview) {
            SettingsOverviewDialog(
                onDismiss = { showSettingsOverview = false },
                onNotifications = {
                    showSettingsOverview = false
                    showNotificationSettings = true
                },
                onSchedule = {
                    showSettingsOverview = false
                    showScheduleSettings = true
                },
                onAppearance = {
                    showSettingsOverview = false
                    showAppearanceSettings = true
                },
                onAbout = {
                    showSettingsOverview = false
                    showAboutSettings = true
                }
            )
        }
        if (showNotificationSettings) {
            NotificationSettingsDialog(viewModel = viewModel, onDismiss = { showNotificationSettings = false })
        }
        if (showScheduleSettings) {
            ScheduleSettingsDialog(viewModel = viewModel, onDismiss = { showScheduleSettings = false })
        }
        if (showAppearanceSettings) {
            AppearanceSettingsDialog(themeMode, onThemeModeChange, onDismiss = { showAppearanceSettings = false })
        }
        if (showAboutSettings) AboutSettingsDialog(onDismiss = { showAboutSettings = false })

        importPreview?.let { preview ->
            ScheduleImportPreviewDialog(
                preview = preview,
                onConfirm = { viewModel.confirmImport() },
                onCancel = { viewModel.cancelImport() }
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
    onEditClass: (CourseClass, String) -> Unit,
    onAddClass: () -> Unit,
    onImportPdf: () -> Unit,
    onClearAll: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit = {}
) {
    val currentStatus by viewModel.liveStatus.collectAsState()
    val weekSchedule by viewModel.weekSchedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var weekStart by remember {
        mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }

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
                selectedDate = selectedDate,
                weekStart = weekStart,
                selectedDay = selectedDay,
                onDateSelected = { date, dayName ->
                    selectedDate = date
                    weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    onSelectedDayChanged(dayName)
                },
                onPreviousWeek = {
                    val newWeekStart = weekStart.minusWeeks(1)
                    weekStart = newWeekStart
                    val dayIndex = ScheduleConstants.DaysOfWeek.indexOfFirst { it.equals(selectedDay, ignoreCase = true) }.coerceAtLeast(0)
                    selectedDate = newWeekStart.plusDays(dayIndex.toLong())
                },
                onNextWeek = {
                    val newWeekStart = weekStart.plusWeeks(1)
                    weekStart = newWeekStart
                    val dayIndex = ScheduleConstants.DaysOfWeek.indexOfFirst { it.equals(selectedDay, ignoreCase = true) }.coerceAtLeast(0)
                    selectedDate = newWeekStart.plusDays(dayIndex.toLong())
                },
                onTodayClick = {
                    val today = LocalDate.now()
                    selectedDate = today
                    weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val dayName = ScheduleConstants.DaysOfWeek.firstOrNull { it.equals(today.dayOfWeek.name, ignoreCase = true) } ?: "Monday"
                    onSelectedDayChanged(dayName)
                },
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
                },
                onAddClass = onAddClass,
                onImportPdf = onImportPdf,
                onClearAll = onClearAll,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainScheduleApp(themeMode = "system", onThemeModeChange = {})
}
