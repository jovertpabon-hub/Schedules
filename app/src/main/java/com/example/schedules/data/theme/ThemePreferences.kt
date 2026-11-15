package com.example.schedules.data.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.schedules.ui.theme.AppTheme
import com.example.schedules.ui.theme.ThemeMode
import com.example.schedules.ui.theme.ThemeSettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "parclock_theme_preferences")

object ThemePreferences {
    private val KEY_THEME = stringPreferencesKey("selected_theme")
    private val KEY_MODE = stringPreferencesKey("theme_mode")

    fun getThemeSettings(context: Context): Flow<ThemeSettingsData> {
        return context.themeDataStore.data.map { prefs ->
            ThemeSettingsData(
                theme = AppTheme.fromId(prefs[KEY_THEME]),
                mode = ThemeMode.fromId(prefs[KEY_MODE])
            )
        }
    }

    suspend fun setTheme(context: Context, theme: AppTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.id
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_MODE] = mode.id
        }
    }
}
