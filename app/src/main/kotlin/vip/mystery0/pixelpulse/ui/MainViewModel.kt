package vip.mystery0.pixelpulse.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import vip.mystery0.pixelpulse.service.NetworkMonitorService

class MainViewModel(
    private val application: Application,
    private val repository: NetworkRepository
) : AndroidViewModel(application) {

    private val _currentSpeed = MutableStateFlow(NetSpeedData(0, 0))
    val currentSpeed = _currentSpeed.asStateFlow()

    val isShizukuMode = repository.isShizukuMode
    val shizukuPermissionGranted = repository.shizukuPermissionGranted
    val blacklistedInterfaces = repository.blacklistedInterfaces
    val isOverlayEnabled = repository.isOverlayEnabled

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    init {
        startUiPolling()
    }

    private fun startUiPolling() {
        viewModelScope.launch {
            while (true) {
                _currentSpeed.value = repository.getCurrentSpeed()
                delay(1000)
            }
        }
    }

    fun toggleService(enable: Boolean) {
        val intent = Intent(application, NetworkMonitorService::class.java)
        if (enable) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                application.startForegroundService(intent)
            } else {
                application.startService(intent)
            }
            _isServiceRunning.value = true
        } else {
            application.stopService(intent)
            _isServiceRunning.value = false
        }
    }

    fun setShizukuMode(enable: Boolean) {
        repository.setShizukuMode(enable)
    }

    fun setOverlayEnabled(enable: Boolean) {
        repository.setOverlayEnabled(enable)
    }

    fun requestShizukuPermission() {
        repository.requestShizukuPermission()
    }

    fun addToBlacklist(iface: String) {
        val current = blacklistedInterfaces.value.toMutableSet()
        current.add(iface)
        repository.updateBlacklist(current)
    }

    fun removeFromBlacklist(iface: String) {
        val current = blacklistedInterfaces.value.toMutableSet()
        current.remove(iface)
        repository.updateBlacklist(current)
    }
}
