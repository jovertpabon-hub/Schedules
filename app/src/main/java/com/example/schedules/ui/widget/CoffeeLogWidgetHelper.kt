package com.example.schedules.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Centralized helper for updating and synchronizing CoffeeLog Glance Widget instances.
 */
object CoffeeLogWidgetHelper {
    val KEY_REVISION = longPreferencesKey("widget_revision")

    fun updateWidget(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = GlanceAppWidgetManager(appContext)
                val glanceIds = manager.getGlanceIds(CoffeeLogWidget::class.java)
                if (glanceIds.isNotEmpty()) {
                    val timestamp = System.currentTimeMillis()
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(appContext, glanceId) { prefs ->
                            prefs[KEY_REVISION] = timestamp
                        }
                    }
                    CoffeeLogWidget().updateAll(appContext)
                }
            } catch (e: Exception) {
                // Safeguard against refresh exceptions so CRUD operations are not interrupted
            }
        }
    }
}
