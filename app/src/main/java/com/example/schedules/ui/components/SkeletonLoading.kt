package com.example.schedules.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------
// SHIMMER EFFECT
// ---------------------------------------------------------

fun Modifier.shimmer(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = composed {

    val transition = rememberInfiniteTransition(
        label = "SkeletonShimmer"
    )

    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    val baseColor =
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)

    val highlightColor =
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translate - 400f, translate - 400f),
        end = Offset(translate, translate)
    )

    this
        .clip(shape)
        .background(brush)
}

// ---------------------------------------------------------
// REUSABLE SKELETON BOX
// ---------------------------------------------------------

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(8.dp)
) {

    val sizeModifier = if (width != null) {
        modifier.width(width)
    } else {
        modifier.fillMaxWidth()
    }

    Box(
        modifier = sizeModifier
            .height(height)
            .shimmer(shape)
    )
}

// ---------------------------------------------------------
// HEADER SKELETON
// Matches NowTabHeader
// ---------------------------------------------------------

@Composable
fun SkeletonNowHeader() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            SkeletonBox(
                width = 145.dp,
                height = 22.dp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            SkeletonBox(
                width = 125.dp,
                height = 14.dp
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        SkeletonBox(
            width = 105.dp,
            height = 38.dp,
            shape = RoundedCornerShape(50.dp)
        )
    }
}

// ---------------------------------------------------------
// HERO COUNTDOWN SKELETON
// Matches NowTabCountdownCard
// ---------------------------------------------------------

@Composable
fun SkeletonHeroCard(
    modifier: Modifier = Modifier,
    isOngoing: Boolean = true
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(32.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 22.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    width = 115.dp,
                    height = 16.dp
                )

                SkeletonBox(
                    width = 95.dp,
                    height = 26.dp,
                    shape = CircleShape
                )
            }

            // HORIZONTAL TIMER + INFORMATION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // LEFT: CIRCULAR TIMER
                Box(
                    modifier = Modifier.size(125.dp),
                    contentAlignment = Alignment.Center
                ) {

                    if (isOngoing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 9.dp,
                                    color = MaterialTheme.colorScheme
                                        .onSurface.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SkeletonBox(
                            width = 14.dp,
                            height = 14.dp,
                            shape = CircleShape
                        )

                        SkeletonBox(
                            width = 78.dp,
                            height = 23.dp
                        )

                        SkeletonBox(
                            width = 60.dp,
                            height = 10.dp
                        )
                    }
                }

                // RIGHT: CLASS INFORMATION
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(
                        width = 85.dp,
                        height = 12.dp
                    )

                    SkeletonBox(
                        width = 100.dp,
                        height = 12.dp
                    )

                    SkeletonBox(
                        width = 110.dp,
                        height = 12.dp
                    )
                }
            }

            // BOTTOM SCHEDULE PILL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SkeletonBox(
                    width = 215.dp,
                    height = 32.dp,
                    shape = CircleShape
                )
            }
        }
    }
}

// ---------------------------------------------------------
// SECTION HEADER SKELETON
// Matches Monday Classes / Free Time Toggle
// ---------------------------------------------------------

@Composable
fun SkeletonSectionHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        SkeletonBox(
            width = 155.dp,
            height = 20.dp
        )

        SkeletonBox(
            width = 125.dp,
            height = 30.dp,
            shape = CircleShape
        )
    }
}

// ---------------------------------------------------------
// CLASS CARD SKELETON
// Matches NowTabClassDetails
// ---------------------------------------------------------

@Composable
fun SkeletonItemCard(
    modifier: Modifier = Modifier
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.18f
                ),
                RoundedCornerShape(20.dp)
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            // LEFT ACCENT STRIP

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.12f)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // COURSE CODE + NAME + MENU

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        SkeletonBox(
                            width = 65.dp,
                            height = 22.dp,
                            shape = CircleShape
                        )

                        SkeletonBox(
                            modifier = Modifier.weight(1f),
                            height = 18.dp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    SkeletonBox(
                        width = 20.dp,
                        height = 20.dp,
                        shape = CircleShape
                    )
                }

                // CLASS TIME + EXPAND ICON

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    SkeletonBox(
                        width = 155.dp,
                        height = 16.dp
                    )

                    SkeletonBox(
                        width = 20.dp,
                        height = 20.dp,
                        shape = CircleShape
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// COLLAPSED DAILY SUMMARY DASHBOARD
// Matches DailyOverviewDashboard
// ---------------------------------------------------------

@Composable
fun SkeletonDailyOverviewDashboard() {

    Card(
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.15f
                ),
                RoundedCornerShape(20.dp)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // DASHBOARD TITLE

            SkeletonBox(
                width = 190.dp,
                height = 16.dp
            )

            // THREE STATISTIC CARDS

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                repeat(3) {

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            SkeletonBox(
                                height = 12.dp
                            )

                            SkeletonBox(
                                height = 20.dp
                            )
                        }
                    }
                }
            }

            // DIVIDER

            SkeletonBox(
                height = 1.dp
            )

            // CLASS TIMELINE TITLE

            SkeletonBox(
                width = 110.dp,
                height = 14.dp
            )

            // TIMELINE INDICATORS

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                repeat(3) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        SkeletonBox(
                            width = 12.dp,
                            height = 12.dp,
                            shape = CircleShape
                        )

                        SkeletonBox(
                            width = 55.dp,
                            height = 12.dp
                        )

                        SkeletonBox(
                            width = 45.dp,
                            height = 10.dp
                        )
                    }
                }
            }

            SkeletonBox(
                height = 1.dp
            )

            // CONTEXTUAL STATUS

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                SkeletonBox(
                    width = 24.dp,
                    height = 24.dp,
                    shape = CircleShape
                )

                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 14.dp
                )
            }
        }
    }
}

// ---------------------------------------------------------
// FULL NOW TAB SKELETON
// Matches NowTabScreen
// ---------------------------------------------------------

@Composable
fun SkeletonDashboardLayout(
    modifier: Modifier = Modifier,
    isDetailsExpanded: Boolean = true,
    isTodaySelected: Boolean = true,
    isOngoing: Boolean = true,
    classCount: Int = 3
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // HEADER

        SkeletonNowHeader()

        // HERO

        SkeletonHeroCard(
            isOngoing = isTodaySelected && isOngoing
        )

        // COLLAPSED DASHBOARD

        if (!isDetailsExpanded) {

            SkeletonDailyOverviewDashboard()

        } else {

            // DETAILED SCHEDULE

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                SkeletonSectionHeader()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    repeat(classCount.coerceAtLeast(0)) {

                        SkeletonItemCard()
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------

@Preview(
    showBackground = true,
    name = "Now Tab Skeleton"
)
@Composable
fun SkeletonDashboardLayoutPreview() {

    MaterialTheme {

        Surface {

            SkeletonDashboardLayout(
                isDetailsExpanded = true,
                isTodaySelected = true,
                isOngoing = true,
                classCount = 3
            )
        }
    }
}