package vip.mystery0.pixel.meter.ui.settings

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.pixel.meter.data.repository.NetworkRepository

class SettingsViewModel : ViewModel(), KoinComponent {
    private val repository: NetworkRepository by inject()

    // Overlay Settings
    val isOverlayEnabled = repository.isOverlayEnabled
    val overlayBgColor = repository.overlayBgColor
    val overlayCornerRadius = repository.overlayCornerRadius
    val overlayTextSize = repository.overlayTextSize
    val overlayTextUp = repository.overlayTextUp
    val overlayTextDown = repository.overlayTextDown
    val overlayOrderUpFirst = repository.overlayOrderUpFirst
    val isOverlayLocked = repository.isOverlayLocked

    // Notification Settings
    val isNotificationEnabled = repository.isNotificationEnabled
    val notificationTextUp = repository.notificationTextUp
    val notificationTextDown = repository.notificationTextDown
    val notificationOrderUpFirst = repository.notificationOrderUpFirst
    val notificationDisplayMode = repository.notificationDisplayMode

    // General Settings
    val samplingInterval = repository.samplingInterval

    fun setOverlayEnabled(enabled: Boolean) = repository.setOverlayEnabled(enabled)
    fun setOverlayLocked(locked: Boolean) = repository.setOverlayLocked(locked)

    fun setSamplingInterval(interval: Long) = repository.setSamplingInterval(interval)
    fun setOverlayBgColor(color: Int) = repository.setOverlayBgColor(color)
    fun setOverlayCornerRadius(radius: Int) = repository.setOverlayCornerRadius(radius)
    fun setOverlayTextSize(size: Float) = repository.setOverlayTextSize(size)
    fun setOverlayTextUp(text: String) = repository.setOverlayTextUp(text)
    fun setOverlayTextDown(text: String) = repository.setOverlayTextDown(text)
    fun setOverlayOrderUpFirst(upFirst: Boolean) = repository.setOverlayOrderUpFirst(upFirst)

    fun setNotificationEnabled(enabled: Boolean) = repository.setNotificationEnabled(enabled)
    fun setNotificationTextUp(text: String) = repository.setNotificationTextUp(text)
    fun setNotificationTextDown(text: String) = repository.setNotificationTextDown(text)
    fun setNotificationOrderUpFirst(upFirst: Boolean) =
        repository.setNotificationOrderUpFirst(upFirst)

    fun setNotificationDisplayMode(mode: Int) = repository.setNotificationDisplayMode(mode)
}
