package com.example.schedules.ui.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.ScheduleConstants
import com.example.schedules.data.models.WeekSchedule
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmCoffeeCream
import com.example.schedules.ui.theme.WarmRoast
import java.util.Locale

data class SearchResultItem(
    val course: CourseClass,
    val dayName: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GlobalSearchDialog(
    weekSchedule: WeekSchedule,
    notesMap: Map<Long, String> = emptyMap(),
    onDismiss: () -> Unit,
    onResultSelected: (CourseClass, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val searchResults = remember(searchQuery, weekSchedule, notesMap) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim().lowercase(Locale.ROOT)
            val results = mutableListOf<SearchResultItem>()
            val seenIds = mutableSetOf<String>()

            weekSchedule.days.forEach { dayData ->
                dayData.classes.forEach { course ->
                    val noteText = course.id?.let { notesMap[it] } ?: ""
                    val matches = course.courseCode.lowercase(Locale.ROOT).contains(query) ||
                            course.courseName.lowercase(Locale.ROOT).contains(query) ||
                            course.room.lowercase(Locale.ROOT).contains(query) ||
                            dayData.dayName.lowercase(Locale.ROOT).contains(query) ||
                            noteText.lowercase(Locale.ROOT).contains(query) ||
                            course.startTime.format(ScheduleConstants.TimeFormatter).lowercase(Locale.ROOT).contains(query) ||
                            course.endTime.format(ScheduleConstants.TimeFormatter).lowercase(Locale.ROOT).contains(query)

                    if (matches) {
                        val uniqueKey = "${course.id}-${dayData.dayName}-${course.courseCode}-${course.startTime}"
                        if (seenIds.add(uniqueKey)) {
                            results.add(SearchResultItem(course, dayData.dayName))
                        }
                    }
                }
            }

            // Sort by day of week index, then by start time
            results.sortedWith(
                compareBy(
                    { item -> ScheduleConstants.DaysOfWeek.indexOfFirst { it.equals(item.dayName, ignoreCase = true) }.let { if (it == -1) 99 else it } },
                    { item -> item.course.startTime }
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        val surfaceColor = if (isDark) MaterialTheme.colorScheme.background else Color.Transparent
        val mod = if (isDark) {
            Modifier.fillMaxSize()
        } else {
            Modifier.fillMaxSize()
        }
        Surface(
            modifier = mod,
            color = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Search Bar & Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SteamedMilk, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepEspresso
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search subjects, instructors, rooms...",
                                color = DarkMocha.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SteamedMilk,
                            unfocusedContainerColor = SteamedMilk,
                            disabledContainerColor = SteamedMilk,
                            focusedBorderColor = WarmRoast,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = DarkMocha,
                            unfocusedTextColor = DarkMocha
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = DeepEspresso
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = DarkMocha.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }

                // Results or Initial State
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = WarmRoast.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Search your timetable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkMocha
                            )
                            Text(
                                text = "Find subjects, room numbers, times, or notes instantly across all days.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkMocha.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No matching classes found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkMocha
                            )
                            Text(
                                text = "Try searching for another subject, room, or keyword.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkMocha.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(searchResults) { item ->
                            SearchResultCard(
                                item = item,
                                noteText = notesMap[item.course.id],
                                onClick = {
                                    onResultSelected(item.course, item.dayName)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    noteText: String?,
    onClick: () -> Unit
) {
    val timeFormatted = remember(item.course) {
        val startStr = item.course.startTime.format(ScheduleConstants.TimeFormatter)
        val endStr = item.course.endTime.format(ScheduleConstants.TimeFormatter)
        "$startStr – $endStr"
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WarmCoffeeCream),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.25.dp, WarmRoast.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.course.courseCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmRoast
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepEspresso
                ) {
                    Text(
                        text = item.dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SteamedMilk,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = item.course.courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkMocha
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = DeepEspresso,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkMocha.copy(alpha = 0.8f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DeepEspresso,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.course.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkMocha.copy(alpha = 0.8f)
                    )
                }
            }

            if (!noteText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SteamedMilk.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: $noteText",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkMocha,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
