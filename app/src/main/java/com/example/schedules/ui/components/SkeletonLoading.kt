package com.example.schedules.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.schedules.ui.theme.AppBackground
import com.example.schedules.ui.theme.DarkMocha
import com.example.schedules.ui.theme.DeepEspresso
import com.example.schedules.ui.theme.GoldenCrema
import com.example.schedules.ui.theme.SteamedMilk
import com.example.schedules.ui.theme.WarmCoffeeCream

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

    val baseColor = DarkMocha.copy(alpha = 0.08f)
    val highlightColor = DarkMocha.copy(alpha = 0.20f)

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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SkeletonBox(
            width = 160.dp,
            height = 24.dp
        )

        Spacer(modifier = Modifier.height(6.dp))

        SkeletonBox(
            width = 130.dp,
            height = 14.dp
        )
    }
}

// ---------------------------------------------------------
// WEEKLY CALENDAR STRIP SKELETON
// Matches WeeklyCalendarStrip
// ---------------------------------------------------------

@Composable
fun SkeletonWeeklyCalendarStrip() {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarmCoffeeCream),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with title "This week" and calendar icon placeholder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    width = 18.dp,
                    height = 18.dp,
                    shape = CircleShape
                )
                SkeletonBox(
                    width = 70.dp,
                    height = 16.dp
                )
            }

            // 7 day items in one horizontal row matching WeeklyCalendarStrip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { index ->
                    val isSelected = (index == 0)
                    val tileBg = if (isSelected) DeepEspresso else SteamedMilk

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tileBg,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SkeletonBox(
                                width = 24.dp,
                                height = 10.dp,
                                shape = RoundedCornerShape(4.dp)
                            )

                            SkeletonBox(
                                width = 18.dp,
                                height = 16.dp,
                                shape = RoundedCornerShape(4.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(GoldenCrema)
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

// ---------------------------------------------------------
// HERO COUNTDOWN SKELETON
// Matches NowTabCountdownCard
// ---------------------------------------------------------

@Composable
fun SkeletonHeroCard(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DeepEspresso
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                GoldenCrema.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    width = 140.dp,
                    height = 16.dp
                )

                SkeletonBox(
                    width = 60.dp,
                    height = 20.dp,
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // HORIZONTAL TIMER + INFORMATION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // LEFT: CIRCULAR TIMER PLACEHOLDER
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 6.dp,
                                color = GoldenCrema.copy(alpha = 0.25f),
                                shape = CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SkeletonBox(
                            width = 58.dp,
                            height = 18.dp
                        )

                        SkeletonBox(
                            width = 44.dp,
                            height = 10.dp
                        )
                    }
                }

                // RIGHT: CLASS INFORMATION
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBox(
                        width = 140.dp,
                        height = 18.dp
                    )

                    SkeletonBox(
                        width = 110.dp,
                        height = 14.dp
                    )

                    SkeletonBox(
                        width = 75.dp,
                        height = 14.dp
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// SECTION HEADER SKELETON
// Matches Section Header & Toggle Button
// ---------------------------------------------------------

@Composable
fun SkeletonSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBox(
            width = 150.dp,
            height = 20.dp
        )

        SkeletonBox(
            width = 96.dp,
            height = 28.dp,
            shape = RoundedCornerShape(50.dp)
        )
    }
}

// ---------------------------------------------------------
// CLASS CARD SKELETON
// Matches NowTabClassDetails
// ---------------------------------------------------------

@Composable
fun SkeletonClassCard(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SteamedMilk
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        modifier = modifier.fillMaxWidth()
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
                    .background(GoldenCrema)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ROW 1: COURSE CODE + NAME + MENU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBox(
                            width = 60.dp,
                            height = 22.dp,
                            shape = RoundedCornerShape(6.dp)
                        )

                        SkeletonBox(
                            modifier = Modifier.weight(1f),
                            height = 18.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    SkeletonBox(
                        width = 20.dp,
                        height = 20.dp,
                        shape = CircleShape
                    )
                }

                // ROW 2: TIME INFO
                SkeletonBox(
                    width = 140.dp,
                    height = 14.dp
                )

                // ROW 3: ROOM LOCATION + STATUS PILL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        width = 80.dp,
                        height = 14.dp
                    )

                    SkeletonBox(
                        width = 64.dp,
                        height = 18.dp,
                        shape = RoundedCornerShape(50.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// FULL NOW TAB SKELETON LAYOUT
// Matches NowTabScreen
// ---------------------------------------------------------

@Composable
fun SkeletonDashboardLayout(
    modifier: Modifier = Modifier,
    classCount: Int = 3
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER
            SkeletonNowHeader()

            // 1b. WEEKLY CALENDAR STRIP
            SkeletonWeeklyCalendarStrip()

            // 2. HERO COUNTDOWN CARD
            SkeletonHeroCard()

            // 3. CLASS SCHEDULE SECTION
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkeletonSectionHeader()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(classCount.coerceAtLeast(1)) {
                        SkeletonClassCard()
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
                classCount = 3
            )
        }
    }
}
