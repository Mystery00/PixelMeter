package vip.mystery0.pixel.meter.ui.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import android.os.PowerManager as AndroidPowerManager

class SettingsViewModel(
    private val application: Application,
) : AndroidViewModel(application), KoinComponent {
    private val repository: NetworkRepository by inject()
    private val powerManager: AndroidPowerManager by inject()

    val canOverlay = MutableStateFlow(true)
    val hasNotificationPermission = MutableStateFlow(true)
    val isIgnoringBatteryOptimizations = MutableStateFlow(true)
    val canEnableAutoStart = MutableStateFlow(false)

    val isServiceRunning = repository.isMonitoring

    // Overlay Settings
    val isOverlayEnabled = repository.isOverlayEnabled
    val overlayBgColor = repository.overlayBgColor
    val overlayTextColor = repository.overlayTextColor
    val overlayCornerRadius = repository.overlayCornerRadius
    val overlayTextSize = repository.overlayTextSize
    val overlayTextUp = repository.overlayTextUp
    val overlayTextDown = repository.overlayTextDown
    val overlayOrderUpFirst = repository.overlayOrderUpFirst
    val isOverlayLocked = repository.isOverlayLocked

    // Notification Settings
    val isNotificationEnabled = repository.isNotificationEnabled
    val isLiveUpdateEnabled = repository.isLiveUpdateEnabled
    val notificationTextUp = repository.notificationTextUp
    val notificationTextDown = repository.notificationTextDown
    val notificationOrderUpFirst = repository.notificationOrderUpFirst
    val notificationDisplayMode = repository.notificationDisplayMode
    val notificationTextSize = repository.notificationTextSize
    val notificationUnitSize = repository.notificationUnitSize
    val notificationThreshold = repository.notificationThreshold
    val notificationLowTrafficMode = repository.notificationLowTrafficMode
    val notificationUseCustomColor = repository.notificationUseCustomColor
    val notificationColor = repository.notificationColor

    // General Settings
    val samplingInterval = repository.samplingInterval
    val isHideFromRecents = repository.isHideFromRecents
    val isOverlayUseDefaultColors = repository.isOverlayUseDefaultColors
    val isAutoStartServiceEnabled = repository.isAutoStartServiceEnabled

    init {
        refreshOverlaySettings()
    }

    fun refreshOverlaySettings() {
        viewModelScope.launch {
            canOverlay.value = Settings.canDrawOverlays(application)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasNotificationPermission.value = ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                hasNotificationPermission.value = true
            }
            isIgnoringBatteryOptimizations.value =
                powerManager.isIgnoringBatteryOptimizations(application.packageName)
            checkAutoStartPermissions()
        }
    }

    private fun checkAutoStartPermissions() {
        val hasOverlayPermission = canOverlay.value
        val hasNotificationPermission = hasNotificationPermission.value

        val canEnable = hasOverlayPermission || hasNotificationPermission
        canEnableAutoStart.value = canEnable

        if (!canEnable && repository.isAutoStartServiceEnabled.value) {
            setAutoStartServiceEnabled(false)
        }
    }

    fun setOverlayEnabled(enabled: Boolean) = repository.setOverlayEnabled(enabled)
    fun setOverlayLocked(locked: Boolean) = repository.setOverlayLocked(locked)

    fun setSamplingInterval(interval: Long) = repository.setSamplingInterval(interval)
    fun setHideFromRecents(hide: Boolean) = repository.setHideFromRecents(hide)
    fun setOverlayBgColor(color: Int) = repository.setOverlayBgColor(color)
    fun setOverlayTextColor(color: Int) = repository.setOverlayTextColor(color)
    fun setOverlayCornerRadius(radius: Int) = repository.setOverlayCornerRadius(radius)
    fun setOverlayTextSize(size: Float) = repository.setOverlayTextSize(size)
    fun setOverlayTextUp(text: String) = repository.setOverlayTextUp(text)
    fun setOverlayTextDown(text: String) = repository.setOverlayTextDown(text)
    fun setOverlayOrderUpFirst(upFirst: Boolean) = repository.setOverlayOrderUpFirst(upFirst)

    fun setNotificationEnabled(enabled: Boolean) = repository.setNotificationEnabled(enabled)
    fun setLiveUpdateEnabled(enabled: Boolean) = repository.setLiveUpdateEnabled(enabled)
    fun setNotificationTextUp(text: String) = repository.setNotificationTextUp(text)
    fun setNotificationTextDown(text: String) = repository.setNotificationTextDown(text)
    fun setNotificationOrderUpFirst(upFirst: Boolean) =
        repository.setNotificationOrderUpFirst(upFirst)

    fun setNotificationDisplayMode(mode: Int) = repository.setNotificationDisplayMode(mode)
    fun setNotificationTextSize(size: Float) = repository.setNotificationTextSize(size)
    fun setNotificationUnitSize(size: Float) = repository.setNotificationUnitSize(size)
    fun setOverlayUseDefaultColors(useDefault: Boolean) =
        repository.setOverlayUseDefaultColors(useDefault)
    fun setNotificationThreshold(threshold: Long) = repository.setNotificationThreshold(threshold)
    fun setNotificationLowTrafficMode(mode: Int) = repository.setNotificationLowTrafficMode(mode)
    fun setNotificationUseCustomColor(useCustom: Boolean) =
        repository.setNotificationUseCustomColor(useCustom)

    fun setNotificationColor(color: Int) = repository.setNotificationColor(color)

    fun setAutoStartServiceEnabled(enabled: Boolean) =
        repository.setAutoStartServiceEnabled(enabled)
}
