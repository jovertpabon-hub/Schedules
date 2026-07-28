package com.example.schedules.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schedules.data.models.DayScheduleData
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TemplateMatrixTab(weekData: List<DayScheduleData>) {
    val periodsWithTime = listOf(
        "Morning" to "8:00 AM - 12:00PM",
        "Afternoon" to "12:00PM - 5:00PM",
        "Evening" to "5:00PM - 8:00PM"
    )

    var cardState by remember { mutableStateOf(CardDetailState.GRID_LIST) }
    val pagerState = rememberPagerState(initialPage = 0) { weekData.size }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = cardState == CardDetailState.PAGER_DETAIL) {
        cardState = CardDetailState.GRID_LIST
    }

    AnimatedContent(
        targetState = cardState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TemplateViewTransition"
    ) { state ->
        when (state) {
            CardDetailState.GRID_LIST -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxSize()
                                .border(0.5.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        periodsWithTime.forEach { (period, timeRange) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = period.uppercase(Locale.US),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = timeRange,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    weekData.forEachIndexed { index, dayData ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable {
                                    coroutineScope.launch { pagerState.scrollToPage(index) }
                                    cardState = CardDetailState.PAGER_DETAIL
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayData.dayName.take(3).uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            periodsWithTime.forEach { (period, _) ->
                                val matchingClasses = dayData.classes.filter { course ->
                                    val hour = course.startTime.hour
                                    when (period) {
                                        "Morning" -> hour < 12
                                        "Afternoon" -> hour in 12..16
                                        "Evening" -> hour >= 17
                                        else -> false
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        .padding(1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (matchingClasses.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            matchingClasses.forEach { item ->
                                                Text(
                                                    text = item.courseCode,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            CardDetailState.PAGER_DETAIL -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    DetailedDayCard(dayData = weekData[page])
                }
            }
        }
    }
}
