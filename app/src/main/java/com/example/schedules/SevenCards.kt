package com.example.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.models.ScheduleTab
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp(
    viewModel: ScheduleViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { ScheduleTab.entries.size })
    val coroutineScope = rememberCoroutineScope()

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f),
                userScrollEnabled = false,
                beyondViewportPageCount = 1 ,
            ) { page ->
                TabContent(page, viewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TabContent(page: Int, viewModel: ScheduleViewModel) {
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
            CardsViewTab(weekSchedule = weekData)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    MainScheduleApp()
}
