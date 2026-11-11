import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmRoast

@Composable
fun NowTabHeader(
    onAddClass: () -> Unit = {},
    onImportPdf: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showHeaderMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy( 1.dp)
            ) {
                Text(
                    text = "scofe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkMocha,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddClass()
                },
                shape = CircleShape,
                color = SteamedMilk.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.35f)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Class",
                        tint = DeepEspresso,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSearchClick()
                },
                shape = CircleShape,
                color = SteamedMilk.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.35f)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DeepEspresso,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showHeaderMenu = true
                    },
                    shape = CircleShape,
                    color = SteamedMilk,
                    border = BorderStroke(1.dp, WarmRoast.copy(alpha = 0.35f)),
                    modifier = Modifier.size(36.dp),
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = DeepEspresso,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showHeaderMenu,
                    onDismissRequest = { showHeaderMenu = false },
                    modifier = Modifier.background(SteamedMilk)
                ) {
                    DropdownMenuItem(
                        text = { Text("Import COE", fontWeight = FontWeight.Bold, color = DarkMocha) },
                        onClick = {
                            showHeaderMenu = false
                            onImportPdf()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DarkMocha, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear All", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showHeaderMenu = false
                            onClearAll()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", fontWeight = FontWeight.Bold, color = DarkMocha) },
                        onClick = {
                            showHeaderMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = DarkMocha, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}
