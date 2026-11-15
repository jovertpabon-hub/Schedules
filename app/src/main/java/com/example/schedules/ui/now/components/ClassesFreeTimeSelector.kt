package com.example.schedules.ui.now.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ClassesFreeTimeSelector(
    showVacantTimes: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme

    val options = listOf(
        false to ("Classes" to Icons.Default.Book),
        true to ("Free Time" to Icons.Default.FreeBreakfast)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.25.dp,
                color = colors.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, option) ->
            val (label, icon) = option
            val isSelected = showVacantTimes == value

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) colors.primaryContainer else Color.Transparent,
                animationSpec = tween(200),
                label = "segmentBackground"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) colors.onPrimaryContainer
                else colors.onSurfaceVariant.copy(alpha = 0.7f),
                animationSpec = tween(200),
                label = "segmentContent"
            )

            Surface(
                onClick = {
                    if (!isSelected) {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                        onSelectionChanged(value)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(9.dp),
                color = backgroundColor,
                border = if (isSelected) BorderStroke(1.dp, colors.secondary.copy(alpha = 0.6f)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 11.dp
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        color = contentColor
                    )
                }
            }
        }
    }
}
