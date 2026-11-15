package com.example.schedules.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val id: String,
    val displayName: String,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewSurface: Color,
    val previewContainer: Color
) {
    COFFEE(
        id = "coffee",
        displayName = "Coffee",
        lightColorScheme = CoffeeLightColorScheme,
        darkColorScheme = CoffeeDarkColorScheme,
        previewPrimary = WarmRoast,
        previewSecondary = GoldenCrema,
        previewSurface = WarmCoffeeCream,
        previewContainer = DeepEspresso
    ),
    SLATE_BLUE(
        id = "slate_blue",
        displayName = "Slate Blue",
        lightColorScheme = SlateBlueLightColorScheme,
        darkColorScheme = SlateBlueDarkColorScheme,
        previewPrimary = Color(0xFF2563EB),
        previewSecondary = Color(0xFF475569),
        previewSurface = Color(0xFFF8FAFC),
        previewContainer = Color(0xFF1E40AF)
    ),
    FOREST_GREEN(
        id = "forest_green",
        displayName = "Forest Green",
        lightColorScheme = ForestGreenLightColorScheme,
        darkColorScheme = ForestGreenDarkColorScheme,
        previewPrimary = Color(0xFF166534),
        previewSecondary = Color(0xFF15803D),
        previewSurface = Color(0xFFF6FEF8),
        previewContainer = Color(0xFF14532D)
    ),
    INDIGO_PURPLE(
        id = "indigo_purple",
        displayName = "Indigo Purple",
        lightColorScheme = IndigoPurpleLightColorScheme,
        darkColorScheme = IndigoPurpleDarkColorScheme,
        previewPrimary = Color(0xFF4F46E5),
        previewSecondary = Color(0xFF7C3AED),
        previewSurface = Color(0xFFF5F3FF),
        previewContainer = Color(0xFF3730A3)
    ),
    GRAPHITE(
        id = "graphite",
        displayName = "Graphite",
        lightColorScheme = GraphiteLightColorScheme,
        darkColorScheme = GraphiteDarkColorScheme,
        previewPrimary = Color(0xFF374151),
        previewSecondary = Color(0xFF4B5563),
        previewSurface = Color(0xFFF9FAFB),
        previewContainer = Color(0xFF1F2937)
    ),
    TEAL(
        id = "teal",
        displayName = "Teal",
        lightColorScheme = TealLightColorScheme,
        darkColorScheme = TealDarkColorScheme,
        previewPrimary = Color(0xFF0D9488),
        previewSecondary = Color(0xFF0284C7),
        previewSurface = Color(0xFFF0FDFA),
        previewContainer = Color(0xFF115E59)
    ),
    ROSE(
        id = "rose",
        displayName = "Rose",
        lightColorScheme = RoseLightColorScheme,
        darkColorScheme = RoseDarkColorScheme,
        previewPrimary = Color(0xFFE11D48),
        previewSecondary = Color(0xFFDB2777),
        previewSurface = Color(0xFFFFF5F5),
        previewContainer = Color(0xFF881337)
    );

    companion object {
        fun fromId(id: String?): AppTheme {
            if (id.isNull_or_empty()) return COFFEE
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: COFFEE
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()

enum class ThemeMode(val id: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromId(id: String?): ThemeMode {
            if (id.isNull_or_empty()) return SYSTEM
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class ThemeSettingsData(
    val theme: AppTheme = AppTheme.COFFEE,
    val mode: ThemeMode = ThemeMode.SYSTEM
)

@Composable
fun SchedulesTheme(
    theme: AppTheme = AppTheme.COFFEE,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme() || darkTheme
    }

    val colorScheme = if (useDarkTheme) theme.darkColorScheme else theme.lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
