package com.example.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedules.data.models.ScheduleTab
import com.example.schedules.ui.components.CardsViewTab
import com.example.schedules.ui.components.IncomingScheduleBanner
import com.example.schedules.ui.components.NowTabScreen
import com.example.schedules.ui.components.TemplateMatrixTab
import com.example.schedules.ui.components.TimetableMatrixTab
import com.example.schedules.ui.viewmodel.ScheduleViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScheduleApp(
    viewModel: ScheduleViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(ScheduleTab.NOW) }
    val currentStatus by viewModel.currentStatus.collectAsState()
    val weekData by viewModel.weekData.collectAsState()
    val todayRemainingClasses by viewModel.todayRemainingClasses.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                ScheduleTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
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
            IncomingScheduleBanner(status = currentStatus)

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    ScheduleTab.NOW -> NowTabScreen(
                        status = currentStatus,
                        todayRemainingClasses = todayRemainingClasses
                    )
                    ScheduleTab.GRID -> CardsViewTab(weekData = weekData)
                    ScheduleTab.TIMETABLE -> TimetableMatrixTab(weekData = weekData)
                    ScheduleTab.TEMPLATE -> TemplateMatrixTab(weekData = weekData)
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
