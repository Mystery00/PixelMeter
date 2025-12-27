package vip.mystery0.pixel.meter.ui

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.service.NetworkMonitorService

class MainViewModel(
    private val application: Application,
) : AndroidViewModel(application), KoinComponent {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val repository: NetworkRepository by inject()

    val currentSpeed = repository.netSpeed

    val isOverlayEnabled = repository.isOverlayEnabled
    val isOverlayLocked = repository.isOverlayLocked

    val isLiveUpdateEnabled = repository.isLiveUpdateEnabled
    val isNotificationEnabled = repository.isNotificationEnabled

    val isServiceRunning = repository.isMonitoring

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
        } catch (e: Exception) {
            Log.e(TAG, "startService: start foreground service error", e)
            _serviceStartError.value =
                "Failed to start service: ${e.message}" to Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
    }

    fun stopService() {
        val intent = Intent(application, NetworkMonitorService::class.java)
        application.stopService(intent)
        _serviceStartError.value = null
    }

    fun clearError() {
        _serviceStartError.value = null
    }

    fun setOverlayEnabled(enable: Boolean) {
        repository.setOverlayEnabled(enable)
    }

    fun setOverlayLocked(locked: Boolean) {
        repository.setOverlayLocked(locked)
    }

    fun setLiveUpdateEnabled(enable: Boolean) {
        repository.setLiveUpdateEnabled(enable)
    }

    fun setNotificationEnabled(enable: Boolean) {
        repository.setNotificationEnabled(enable)
    }
}
