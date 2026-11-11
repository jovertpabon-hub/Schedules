package com.example.schedules.ui.now.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.WarmRoast
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CompactLiveClockSection(
    currentTime: LocalTime,
    currentDate: LocalDate
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = currentDate.format(dateFormatter),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = WarmRoast,
            textAlign = TextAlign.Center
        )

        Text(
            text = currentTime.format(timeFormatter),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = DeepEspresso,
            textAlign = TextAlign.Center
        )
    }
}
