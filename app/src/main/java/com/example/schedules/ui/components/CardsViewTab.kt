package com.example.schedules.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedules.data.models.WeekSchedule
import kotlinx.coroutines.launch

@Composable
fun CardsViewTab(weekSchedule: WeekSchedule) {
    var cardState by remember { mutableStateOf(CardDetailState.GRID_LIST) }
    val pagerState = rememberPagerState(initialPage = 0) { weekSchedule.days.size }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = cardState == CardDetailState.PAGER_DETAIL) {
        cardState = CardDetailState.GRID_LIST
    }

    AnimatedContent(
        targetState = cardState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "CardViewTransition"
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
                    DetailedDayCard(dayData = weekSchedule.days[page])
                }
            }
        }
    }
}
