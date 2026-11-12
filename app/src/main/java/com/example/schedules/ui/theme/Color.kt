
package com.example.schedules.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. Foundation Coffee Palette
// ============================================================================

// Primary coffee colors
val DarkMocha = Color(0xFF543020)
val DeepEspresso = Color(0xFF4D2A00)
val WarmRoast = Color(0xFFB85D00)
val GoldenCrema = Color(0xFFD99A16)

// Cream and supporting colors
val WarmCoffeeCream = Color(0xFFF5E6C5)
val SteamedMilk = Color(0xFFDCC58F)

// Main application background
val AppBackground = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFE5CEAA),
        0.25f to Color(0xFFD5B68B),
        0.45f to Color(0xFFB99A75), // Darker middle
        0.60f to Color(0xFFC5A078),
        0.80f to Color(0xFFB38B63),
        1.0f to Color(0xFFA47A55)  // Darker bottom
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

val AppBackgroundBase = Color(0xFFD5B68B)


// ============================================================================
// 2. Dark Theme Surface Hierarchy
// ============================================================================

val SurfaceDark = Color(0xFF1B1009)
val SurfaceVariantDark = Color(0xFF302014)
val SecondaryContainerDark = Color(0xFF493019)


// ============================================================================
// 3. Light Theme Material 3 Mapping
// ============================================================================

val SurfaceLight = WarmCoffeeCream
val OnSurfaceLight = DarkMocha

val SurfaceVariantLight = SteamedMilk
val OnSurfaceVariantLight = DarkMocha

val PrimaryLight = WarmRoast
val OnPrimaryLight = WarmCoffeeCream

val PrimaryContainerLight = DeepEspresso
val OnPrimaryContainerLight = WarmCoffeeCream

val SecondaryLight = GoldenCrema
val OnSecondaryLight = DeepEspresso

val SecondaryContainerLight = SteamedMilk
val OnSecondaryContainerLight = DarkMocha

val OutlineLight = DarkMocha
val OutlineVariantLight = Color(0xFFC5A078)


// ============================================================================
// 4. Dark Theme Material 3 Mapping
// ============================================================================

val OnSurfaceDark = WarmCoffeeCream
val OnSurfaceVariantDark = SteamedMilk

val PrimaryDark = WarmRoast
val OnPrimaryDark = SurfaceDark

val PrimaryContainerDark = DeepEspresso
val OnPrimaryContainerDark = WarmCoffeeCream

val SecondaryDark = GoldenCrema
val OnSecondaryDark = DeepEspresso

val OnSecondaryContainerDark = WarmCoffeeCream

val OutlineDark = WarmRoast
val OutlineVariantDark = DeepEspresso
