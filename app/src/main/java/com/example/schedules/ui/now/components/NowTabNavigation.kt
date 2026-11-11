package com.example.schedules.ui.now.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.schedules.ui.now.model.NowSubTab
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso

@Composable
fun NowTabSubNavigation(
    selectedSubTab: NowSubTab,
    onSubTabSelected: (NowSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NowSubTab.entries.forEach { tab ->
            val isSelected = tab == selectedSubTab

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) DeepEspresso else DarkMocha.copy(alpha = 0.45f),
                animationSpec = tween(durationMillis = 150),
                label = "tabIconColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) DeepEspresso else DarkMocha.copy(alpha = 0.55f),
                animationSpec = tween(durationMillis = 150),
                label = "tabTextColor"
            )

            val iconVector = when (tab) {
                NowSubTab.TODAY -> Icons.Default.WbSunny
                NowSubTab.WEEKLY_PLANNER -> Icons.Default.CalendarMonth
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClick = {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSubTabSelected(tab)
                            }
                        }
                    )
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = tab.title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
