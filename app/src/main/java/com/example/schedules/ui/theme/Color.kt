package com.example.schedules.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. Foundation Coffee Palette (Legacy & Default Theme)
// ============================================================================

val DarkMocha = Color(0xFF543020)
val DeepEspresso = Color(0xFF4D2A00)
val WarmRoast = Color(0xFFB85D00)
val GoldenCrema = Color(0xFFD99A16)

val WarmCoffeeCream = Color(0xFFF5E6C5)
val SteamedMilk = Color(0xFFDCC58F)

val AppBackground = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFE5CEAA),
        0.25f to Color(0xFFD5B68B),
        0.45f to Color(0xFFB99A75),
        0.60f to Color(0xFFC5A078),
        0.80f to Color(0xFFB38B63),
        1.0f to Color(0xFFA47A55)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

val AppBackgroundBase = Color(0xFFD5B68B)
val SurfaceDark = Color(0xFF1B1009)
val SurfaceVariantDark = Color(0xFF302014)
val SecondaryContainerDark = Color(0xFF493019)

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

// ----------------------------------------------------------------------------
// 1. COFFEE SCHEMES
// ----------------------------------------------------------------------------
val CoffeeLightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = GoldenCrema,
    onTertiary = DeepEspresso,
    tertiaryContainer = WarmCoffeeCream,
    onTertiaryContainer = DarkMocha,
    background = AppBackgroundBase,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

val CoffeeDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = GoldenCrema,
    onTertiary = DeepEspresso,
    tertiaryContainer = SecondaryContainerDark,
    onTertiaryContainer = SteamedMilk,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// ----------------------------------------------------------------------------
// 2. SLATE BLUE SCHEMES
// ----------------------------------------------------------------------------
val SlateBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF475569),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFFCBD5E1)
)

val SlateBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFF8FAFC),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155)
)

// ----------------------------------------------------------------------------
// 3. FOREST GREEN SCHEMES
// ----------------------------------------------------------------------------
val ForestGreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF166534),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF15803D),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF052E16),
    background = Color(0xFFF0FDF4),
    onBackground = Color(0xFF052E16),
    surface = Color(0xFFF6FEF8),
    onSurface = Color(0xFF052E16),
    surfaceVariant = Color(0xFFDCFCE7),
    onSurfaceVariant = Color(0xFF166534),
    outline = Color(0xFF22C55E),
    outlineVariant = Color(0xFF86EFAC)
)

val ForestGreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF052E16),
    secondary = Color(0xFF86EFAC),
    onSecondary = Color(0xFF14532D),
    primaryContainer = Color(0xFF166534),
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondaryContainer = Color(0xFF143D23),
    onSecondaryContainer = Color(0xFFF0FDF4),
    background = Color(0xFF051C10),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0A2917),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF143D23),
    onSurfaceVariant = Color(0xFF86EFAC),
    outline = Color(0xFF22C55E),
    outlineVariant = Color(0xFF14532D)
)

// ----------------------------------------------------------------------------
// 4. INDIGO PURPLE SCHEMES
// ----------------------------------------------------------------------------
val IndigoPurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF1E1B4B),
    background = Color(0xFFEEF2FF),
    onBackground = Color(0xFF1E1B4B),
    surface = Color(0xFFF5F3FF),
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = Color(0xFFE0E7FF),
    onSurfaceVariant = Color(0xFF3730A3),
    outline = Color(0xFF6366F1),
    outlineVariant = Color(0xFFC7D2FE)
)

val IndigoPurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF2E1065),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondaryContainer = Color(0xFF272459),
    onSecondaryContainer = Color(0xFFEEF2FF),
    background = Color(0xFF0F0E26),
    onBackground = Color(0xFFEEF2FF),
    surface = Color(0xFF181638),
    onSurface = Color(0xFFEEF2FF),
    surfaceVariant = Color(0xFF272459),
    onSurfaceVariant = Color(0xFFC7D2FE),
    outline = Color(0xFF6366F1),
    outlineVariant = Color(0xFF3730A3)
)

// ----------------------------------------------------------------------------
// 5. GRAPHITE SCHEMES
// ----------------------------------------------------------------------------
val GraphiteLightColorScheme = lightColorScheme(
    primary = Color(0xFF374151),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4B5563),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1F2937),
    onPrimaryContainer = Color(0xFFF3F4F6),
    secondaryContainer = Color(0xFFE5E7EB),
    onSecondaryContainer = Color(0xFF111827),
    background = Color(0xFFE5E7EB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFF9FAFB),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF374151),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFFD1D5DB)
)

val GraphiteDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9CA3AF),
    onPrimary = Color(0xFF111827),
    secondary = Color(0xFFD1D5DB),
    onSecondary = Color(0xFF1F2937),
    primaryContainer = Color(0xFF374151),
    onPrimaryContainer = Color(0xFFF3F4F6),
    secondaryContainer = Color(0xFF21262D),
    onSecondaryContainer = Color(0xFFF3F4F6),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF374151)
)

// ----------------------------------------------------------------------------
// 6. TEAL SCHEMES
// ----------------------------------------------------------------------------
val TealLightColorScheme = lightColorScheme(
    primary = Color(0xFF0D9488),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0284C7),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    background = Color(0xFFE6FFFA),
    onBackground = Color(0xFF134E4A),
    surface = Color(0xFFF0FDFA),
    onSurface = Color(0xFF134E4A),
    surfaceVariant = Color(0xFF99F6E4),
    onSurfaceVariant = Color(0xFF0F766E),
    outline = Color(0xFF0D9488),
    outlineVariant = Color(0xFF5EEAD4)
)

val TealDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF042F2C),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0C4A6E),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondaryContainer = Color(0xFF14524D),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF042F2C),
    onBackground = Color(0xFFCCFBF1),
    surface = Color(0xFF0D3D39),
    onSurface = Color(0xFFCCFBF1),
    surfaceVariant = Color(0xFF14524D),
    onSurfaceVariant = Color(0xFF5EEAD4),
    outline = Color(0xFF14B8A6),
    outlineVariant = Color(0xFF115E59)
)

// ----------------------------------------------------------------------------
// 7. ROSE SCHEMES
// ----------------------------------------------------------------------------
val RoseLightColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFDB2777),
    onSecondary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF881337),
    onPrimaryContainer = Color(0xFFFFE4E6),
    secondaryContainer = Color(0xFFFFE4E6),
    onSecondaryContainer = Color(0xFF4C0519),
    background = Color(0xFFFFF1F2),
    onBackground = Color(0xFF4C0519),
    surface = Color(0xFFFFF5F5),
    onSurface = Color(0xFF4C0519),
    surfaceVariant = Color(0xFFFECDD3),
    onSurfaceVariant = Color(0xFF9F1239),
    outline = Color(0xFFE11D48),
    outlineVariant = Color(0xFFFDA4AF)
)

val RoseDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFB7185),
    onPrimary = Color(0xFF4C0519),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF831843),
    primaryContainer = Color(0xFF9F1239),
    onPrimaryContainer = Color(0xFFFFE4E6),
    secondaryContainer = Color(0xFF581529),
    onSecondaryContainer = Color(0xFFFFE4E6),
    background = Color(0xFF2A0812),
    onBackground = Color(0xFFFFE4E6),
    surface = Color(0xFF3F0D1C),
    onSurface = Color(0xFFFFE4E6),
    surfaceVariant = Color(0xFF581529),
    onSurfaceVariant = Color(0xFFFECDD3),
    outline = Color(0xFFF43F5E),
    outlineVariant = Color(0xFF881337)
)
