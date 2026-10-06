package com.example.schedules.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schedules.data.models.CourseClass
import com.example.schedules.data.models.WeekSchedule
import kotlinx.coroutines.launch

@Composable
fun CardsViewTab(
    weekSchedule: WeekSchedule,
    onEdit: (CourseClass, String) -> Unit = { _, _ -> },
    onDelete: (CourseClass) -> Unit = {},
    onVisibleDayChanged: (String) -> Unit = {},
    onBackToMain: () -> Unit = {}
) {
    var cardState by remember { mutableStateOf(CardDetailState.GRID_LIST) }
    val pagerState = rememberPagerState(initialPage = 0) { weekSchedule.days.size }
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (cardState == CardDetailState.PAGER_DETAIL) {
            cardState = CardDetailState.GRID_LIST
        } else {
            onBackToMain()
        }
    }

    // Phase 2: Report visible day for context-aware adding
    LaunchedEffect(pagerState, cardState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (cardState == CardDetailState.PAGER_DETAIL && page < weekSchedule.days.size) {
                onVisibleDayChanged(weekSchedule.days[page].dayName)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (cardState == CardDetailState.PAGER_DETAIL) {
                        cardState = CardDetailState.GRID_LIST
                    } else {
                        onBackToMain()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = if (cardState == CardDetailState.PAGER_DETAIL) {
                    weekSchedule.days.getOrNull(pagerState.currentPage)?.dayName ?: "Day Detail"
                } else {
                    "Cards View"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedContent(
            targetState = cardState,
            transitionSpec = {
                if (targetState == CardDetailState.PAGER_DETAIL) {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f))
                        .togetherWith(fadeOut(animationSpec = tween(300)))
                } else {
                    fadeIn(animationSpec = tween(300))
                        .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f))
                }
            },
            label = "CardViewTransition",
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                CardDetailState.GRID_LIST -> {
                    val weekData = weekSchedule.days
                    val weekdays = remember(weekData) { weekData.take(6) }
                    val sunday = remember(weekData) { weekData.lastOrNull() }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = weekdays,
                            key = { _, item -> item.dayName }
                        ) { index, dayData ->
                            DayMiniCard(
                                dayData = dayData,
                                onClick = {
                                    coroutineScope.launch { pagerState.scrollToPage(index) }
                                    cardState = CardDetailState.PAGER_DETAIL
                                }
                            )
                        }
                        if (sunday != null) {
                            item(
                                span = { GridItemSpan(2) },
                                key = "Sunday"
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DayMiniCard(
                                        dayData = sunday,
                                        modifier = Modifier.fillMaxWidth(0.5f),
                                        onClick = {
                                            coroutineScope.launch { pagerState.scrollToPage(6) }
                                            cardState = CardDetailState.PAGER_DETAIL
                                        }
                                    )
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
                        val dayData = weekSchedule.days[page]
                        DetailedDayCard(
                            dayData = dayData,
                            onEdit = { onEdit(it, dayData.dayName) },
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}
