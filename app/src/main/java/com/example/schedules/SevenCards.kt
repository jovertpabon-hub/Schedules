package com.example.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.ScheduleTab
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.viewmodel.ScheduleCrudViewModel
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp() {
    val context = LocalContext.current
    val repository = (context.applicationContext as SchedulesApplication).repository
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.Factory(repository)
    )
    val crudViewModel: ScheduleCrudViewModel = viewModel(
        factory = ScheduleCrudViewModel.Factory(repository)
    )

    var showDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<CourseClass?>(null) }
    var editingDay by remember { mutableStateOf("") }
    
    val pagerState = rememberPagerState(pageCount = { ScheduleTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing by crudViewModel.isRefreshing.collectAsState()

    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    Scaffold(
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
                            onRefresh = { crudViewModel.refresh() },
                            onEdit = { course, day -> 
                                editingClass = course
                                editingDay = day
                                showDialog = true 
                            },
                            onDelete = { it.id?.let { id -> crudViewModel.deleteSchedule(id) } }
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
                    crudViewModel.addOrUpdateSchedule(id, day, code, name, start, end, room, instructor)
                    showDialog = false
                },
                crudViewModel = crudViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddOrEditClassDialog(
    editingClass: CourseClass?,
    editingDay: String,
    onDismiss: () -> Unit,
    onConfirm: (Long?, String, String, String, LocalTime, LocalTime, String, String) -> Unit,
    crudViewModel: ScheduleCrudViewModel
) {
    var day by remember { mutableStateOf(if (editingClass != null) editingDay else "Monday") }
    
    var code by remember { mutableStateOf(editingClass?.courseCode ?: "") }
    var name by remember { mutableStateOf(editingClass?.courseName ?: "") }
    var startTime by remember { mutableStateOf(editingClass?.startTime ?: LocalTime.of(8, 0)) }
    var endTime by remember { mutableStateOf(editingClass?.endTime ?: LocalTime.of(9, 0)) }
    var room by remember { mutableStateOf(editingClass?.room ?: "") }
    var instructor by remember { mutableStateOf(editingClass?.instructor ?: "") }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var expanded by remember { mutableStateOf(false) }

    val validationError by crudViewModel.validationError.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (editingClass == null) "Add New Class" else "Edit Class",
                    style = MaterialTheme.typography.titleLarge
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        days.forEach { selectionDay ->
                            DropdownMenuItem(
                                text = { Text(selectionDay) },
                                onClick = {
                                    day = selectionDay
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime.format(ScheduleConstants.TimeFormatter),
                        onValueChange = {},
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                        readOnly = true,
                        enabled = false,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTime.format(ScheduleConstants.TimeFormatter),
                        onValueChange = {},
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                        readOnly = true,
                        enabled = false,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = instructor, onValueChange = { instructor = it }, label = { Text("Instructor") }, modifier = Modifier.fillMaxWidth())

                validationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { 
                        onConfirm(editingClass?.id, day, code, name, startTime, endTime, room, instructor)
                    }) { 
                        Text(if (editingClass == null) "Add" else "Update") 
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { 
                startTime = it
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { 
                endTime = it
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { 
                        onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute)) 
                    }) { Text("OK") }
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
