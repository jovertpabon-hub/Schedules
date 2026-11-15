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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
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
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

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
// FIXED TOP HEADER SKELETON
// Matches NowTabHeaderSection & NowTabHeader
// ---------------------------------------------------------

@Composable
fun SkeletonNowHeaderSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App title placeholder ("clofee")
                    SkeletonBox(
                        width = 110.dp,
                        height = 24.dp,
                        shape = RoundedCornerShape(6.dp)
                    )

                    // Header action buttons (Add, Search, Overflow Menu)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            SkeletonBox(
                                width = 36.dp,
                                height = 36.dp,
                                shape = CircleShape
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ---------------------------------------------------------
// HERO COUNTDOWN / OVERVIEW CARD SKELETON
// Matches NowTabCountdownCard inside HorizontalPager
// ---------------------------------------------------------

@Composable
fun SkeletonHeroCard(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.25.dp,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row (Status & Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    width = 130.dp,
                    height = 16.dp,
                    shape = RoundedCornerShape(4.dp)
                )

                SkeletonBox(
                    width = 60.dp,
                    height = 20.dp,
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // Body Row (Circular timer & class info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Timer Box
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        SkeletonBox(
                            width = 56.dp,
                            height = 16.dp,
                            shape = RoundedCornerShape(4.dp)
                        )

                        SkeletonBox(
                            width = 44.dp,
                            height = 10.dp,
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }

                // Course Information Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBox(
                        width = 150.dp,
                        height = 18.dp,
                        shape = RoundedCornerShape(4.dp)
                    )

                    SkeletonBox(
                        width = 110.dp,
                        height = 14.dp,
                        shape = RoundedCornerShape(4.dp)
                    )

                    SkeletonBox(
                        width = 80.dp,
                        height = 14.dp,
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PAGE INDICATOR SKELETON
// Matches HorizontalPager Indicator
// ---------------------------------------------------------

@Composable
fun SkeletonPageIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBox(
            modifier = Modifier.padding(horizontal = 4.dp),
            width = 22.dp,
            height = 8.dp,
            shape = CircleShape
        )
        SkeletonBox(
            modifier = Modifier.padding(horizontal = 4.dp),
            width = 8.dp,
            height = 8.dp,
            shape = CircleShape
        )
    }
}

// ---------------------------------------------------------
// SHARED SCHEDULE SECTION HEADER SKELETON
// Matches Shared Schedule Header
// ---------------------------------------------------------

@Composable
fun SkeletonScheduleSectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(
                width = 20.dp,
                height = 20.dp,
                shape = RoundedCornerShape(4.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SkeletonBox(
                    width = 135.dp,
                    height = 18.dp,
                    shape = RoundedCornerShape(4.dp)
                )

                SkeletonBox(
                    width = 105.dp,
                    height = 13.dp,
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        SkeletonBox(
            width = 72.dp,
            height = 24.dp,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ---------------------------------------------------------
// SEGMENTED SELECTOR SKELETON
// Matches ClassesFreeTimeSelector
// ---------------------------------------------------------

@Composable
fun SkeletonClassesFreeTimeSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SkeletonBox(
            modifier = Modifier.weight(1f),
            height = 39.dp,
            shape = RoundedCornerShape(9.dp)
        )

        SkeletonBox(
            modifier = Modifier.weight(1f),
            height = 39.dp,
            shape = RoundedCornerShape(9.dp)
        )
    }
}

// ---------------------------------------------------------
// CLASS CARD SKELETON
// Matches NowTabClassDetails / ClassItemCard
// ---------------------------------------------------------

@Composable
fun SkeletonClassCard(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.25.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Course Code, Status Badge, Name & Overflow Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBox(
                            width = 65.dp,
                            height = 18.dp,
                            shape = RoundedCornerShape(4.dp)
                        )

                        SkeletonBox(
                            width = 52.dp,
                            height = 16.dp,
                            shape = RoundedCornerShape(50.dp)
                        )
                    }

                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        height = 16.dp,
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                SkeletonBox(
                    width = 24.dp,
                    height = 24.dp,
                    shape = CircleShape
                )
            }

            // Row 2: Time & Room
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    width = 140.dp,
                    height = 14.dp,
                    shape = RoundedCornerShape(4.dp)
                )

                SkeletonBox(
                    width = 75.dp,
                    height = 14.dp,
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// STANDALONE WEEKLY CALENDAR STRIP SKELETON
// Matches WeeklyCalendarStrip inside Calendar Overview Card
// ---------------------------------------------------------

@Composable
fun SkeletonWeeklyCalendarStrip() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonBox(
                    width = 32.dp,
                    height = 32.dp,
                    shape = CircleShape
                )
                SkeletonBox(
                    width = 120.dp,
                    height = 20.dp,
                    shape = RoundedCornerShape(4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonBox(
                    width = 45.dp,
                    height = 20.dp,
                    shape = RoundedCornerShape(4.dp)
                )
                SkeletonBox(
                    width = 32.dp,
                    height = 32.dp,
                    shape = CircleShape
                )
                SkeletonBox(
                    width = 32.dp,
                    height = 32.dp,
                    shape = CircleShape
                )
            }
        }

        // 7 day items row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(7) { index ->
                val isSelected = (index == 0)
                val tileBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tileBg,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 11.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SkeletonBox(
                            width = 24.dp,
                            height = 11.dp,
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
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// FULL NOW TAB SKELETON LAYOUT
// Matches NowTabScreen Structure 1:1
// ---------------------------------------------------------

@Composable
fun SkeletonDashboardLayout(
    modifier: Modifier = Modifier,
    classCount: Int = 3
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Fixed Top Header Section
            SkeletonNowHeaderSection()

            // 2. Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Overview Section (Hero Card + Indicator grouped together)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp)
                    ) {
                        SkeletonHeroCard()
                    }

                    // Page Indicator
                    SkeletonPageIndicator()
                }

                // Shared Schedule Section Header
                SkeletonScheduleSectionHeader()

                // Classes / Free Time Segmented Selector
                SkeletonClassesFreeTimeSelector()

                // Class Cards List
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
