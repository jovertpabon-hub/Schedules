package com.example.schedules.ui.theme

import androidx.compose.ui.graphics.Color

// 5-Tone Coffee Palette (Lead UI/UX Designer Design System)
val DarkMocha = Color(0xFF623528)          // Primary text, screen titles, dark card borders, primary readable labels
val DeepEspresso = Color(0xFF4D2A00)       // Hero card headers, dark container surfaces, primary icon fills
val WarmRoast = Color(0xFFCC6F00)          // Primary action elements (FAB), active state highlights, focus indicators
val GoldenCrema = Color(0xFFF2A900)        // Progress indicators, status badges, highlighted timer digits
val AppBackground = Color(0xFFD6C0A5)
val WarmCoffeeCream = Color(0xFFFFF4D6)    // Main cards
val SteamedMilk = Color(0xFFF0DDAA)        // Card backgrounds, secondary informational containers, subtle fills

// Light Theme - 5-Tone Coffee Palette Mapping
val SurfaceLight = WarmCoffeeCream
val OnSurfaceLight = DarkMocha
val SurfaceVariantLight = SteamedMilk
val OnSurfaceVariantLight = DarkMocha
val PrimaryLight = WarmRoast
val OnPrimaryLight = Color.White
val PrimaryContainerLight = SteamedMilk
val OnPrimaryContainerLight = DarkMocha
val SecondaryLight = GoldenCrema
val OnSecondaryLight = DeepEspresso
val SecondaryContainerLight = SteamedMilk
val OnSecondaryContainerLight = DarkMocha
val OutlineLight = DarkMocha
val OutlineVariantLight = Color(0xFFD4B982)

// Dark Theme - Rich Espresso Dark Theme Mapping
val SurfaceDark = Color(0xFF1F1206)
val OnSurfaceDark = SteamedMilk
val SurfaceVariantDark = Color(0xFF382104)
val OnSurfaceVariantDark = GoldenCrema
val PrimaryDark = WarmRoast
val OnPrimaryDark = WarmCoffeeCream
val PrimaryContainerDark = DeepEspresso
val OnPrimaryContainerDark = SteamedMilk
val SecondaryDark = GoldenCrema
val OnSecondaryDark = DeepEspresso
val SecondaryContainerDark = Color(0xFF593100)
val OnSecondaryContainerDark = SteamedMilk
val OutlineDark = WarmRoast
val OutlineVariantDark = DeepEspresso
