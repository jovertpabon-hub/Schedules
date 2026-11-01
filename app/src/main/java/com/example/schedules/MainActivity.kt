package com.example.schedules

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import com.example.schedules.ui.theme.SchedulesTheme

class MainActivity : ComponentActivity() {

    private var startupReconciliationPending = false
    private val currentIntentState = mutableStateOf<android.content.Intent?>(null)

    companion object {
        private const val KEY_STARTUP_RECONCILIATION_PENDING = "startup_reconciliation_pending"
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldReconcile = isGranted && startupReconciliationPending
        startupReconciliationPending = false
        if (shouldReconcile) {
            ScheduleNotificationHelper.rescheduleAllReminders(this)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState.value = intent
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentState.value = intent
        startupReconciliationPending = savedInstanceState?.getBoolean(KEY_STARTUP_RECONCILIATION_PENDING)
            ?: checkAndRequestNotificationPermission()
        if (savedInstanceState == null && !startupReconciliationPending) {
            ScheduleNotificationHelper.rescheduleAllReminders(this)
        }

        setContent {
            val preferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
            var themeMode by remember {
                mutableStateOf(preferences.getString("theme_mode", "system") ?: "system")
            }
            SchedulesTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScheduleApp(
                        themeMode = themeMode,
                        onThemeModeChange = { selected ->
                            themeMode = selected
                            preferences.edit().putString("theme_mode", selected).apply()
                        },
                        intent = currentIntentState.value
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_STARTUP_RECONCILIATION_PENDING, startupReconciliationPending)
        super.onSaveInstanceState(outState)
    }

    private fun checkAndRequestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return true
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainScheduleAppPreview() {
    MainScheduleApp(themeMode = "light", onThemeModeChange = {})
}
