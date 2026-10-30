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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.schedules.data.notifications.ScheduleNotificationHelper
import com.example.schedules.ui.theme.SchedulesTheme

class MainActivity : ComponentActivity() {

    private var startupReconciliationPending = false

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startupReconciliationPending = savedInstanceState?.getBoolean(KEY_STARTUP_RECONCILIATION_PENDING)
            ?: checkAndRequestNotificationPermission()
        if (savedInstanceState == null && !startupReconciliationPending) {
            ScheduleNotificationHelper.rescheduleAllReminders(this)
        }

        setContent {
            SchedulesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScheduleApp()
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
    MainScheduleApp()
}
