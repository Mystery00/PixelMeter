package vip.mystery0.pixelpulse.ui

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.service.NetworkMonitorService

class MainViewModel(
    private val application: Application,
) : AndroidViewModel(application), KoinComponent {
    private val repository: NetworkRepository by inject()

    val currentSpeed = repository.netSpeed

    val isOverlayEnabled = repository.isOverlayEnabled

    val isLiveUpdateEnabled = repository.isLiveUpdateEnabled

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    private val _serviceStartError = MutableStateFlow<Pair<String, String>?>(null)
    val serviceStartError = _serviceStartError.asStateFlow()

    fun startService() {
        _serviceStartError.value = null

        // 1. Check Notification Permission (Android 13+)
        if (ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _serviceStartError.value =
                "Notification permission required" to Settings.ACTION_APP_NOTIFICATION_SETTINGS
            return
        }

        // 2. Check Overlay Permission if enabled
        if (isOverlayEnabled.value) {
            if (!Settings.canDrawOverlays(application)) {
                _serviceStartError.value =
                    "Overlay permission required for Floating Window" to Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                return
            }
        }

        val intent = Intent(application, NetworkMonitorService::class.java)
        try {
            application.startForegroundService(intent)
            _isServiceRunning.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _serviceStartError.value =
                "Failed to start service: ${e.message}" to Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            _isServiceRunning.value = false
        }
    }

    fun stopService() {
        val intent = Intent(application, NetworkMonitorService::class.java)
        application.stopService(intent)
        _isServiceRunning.value = false
        _serviceStartError.value = null
    }

    fun clearError() {
        _serviceStartError.value = null
    }

    fun setOverlayEnabled(enable: Boolean) {
        repository.setOverlayEnabled(enable)
    }

    fun setLiveUpdateEnabled(enable: Boolean) {
        repository.setLiveUpdateEnabled(enable)
    }
}
