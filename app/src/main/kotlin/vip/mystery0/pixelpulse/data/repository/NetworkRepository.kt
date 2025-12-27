package vip.mystery0.pixelpulse.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import vip.mystery0.pixelpulse.data.source.impl.SpeedDataSource
import java.util.Locale

class NetworkRepository(
    private val standardDataSource: SpeedDataSource,
    private val prefs: SharedPreferences
) : KoinComponent {
    private val _isOverlayEnabled = MutableStateFlow(false)
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isLiveUpdateEnabled = MutableStateFlow(false)
    val isLiveUpdateEnabled: StateFlow<Boolean> = _isLiveUpdateEnabled.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isOverlayLocked = MutableStateFlow(false)
    val isOverlayLocked: StateFlow<Boolean> = _isOverlayLocked.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _netSpeed = MutableStateFlow(NetSpeedData(0, 0))
    val netSpeed: StateFlow<NetSpeedData> = _netSpeed.asStateFlow()

    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L

    init {
        _isLiveUpdateEnabled.value = prefs.getBoolean("key_live_update", false)
        _isNotificationEnabled.value = prefs.getBoolean("key_notification_enabled", true)
        _isOverlayLocked.value = prefs.getBoolean("key_overlay_locked", false)
    }

    fun setOverlayEnabled(enable: Boolean) {
        _isOverlayEnabled.value = enable
    }

    fun setLiveUpdateEnabled(enable: Boolean) {
        _isLiveUpdateEnabled.value = enable
        prefs.edit { putBoolean("key_live_update", enable) }
    }

    fun setNotificationEnabled(enable: Boolean) {
        _isNotificationEnabled.value = enable
        prefs.edit { putBoolean("key_notification_enabled", enable) }
    }

    fun setOverlayLocked(locked: Boolean) {
        _isOverlayLocked.value = locked
        prefs.edit { putBoolean("key_overlay_locked", locked) }
    }

    fun getOverlayPosition(): Pair<Int, Int> {
        val x = prefs.getInt("key_overlay_x", 100)
        val y = prefs.getInt("key_overlay_y", 200)
        return x to y
    }

    fun saveOverlayPosition(x: Int, y: Int) {
        prefs.edit {
            putInt("key_overlay_x", x)
            putInt("key_overlay_y", y)
        }
    }

    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        // Reset state
        lastTotalRxBytes = 0L
        lastTotalTxBytes = 0L
        lastTime = 0L

        _isMonitoring.value = true

        monitoringJob = scope.launch {
            while (isActive) {
                val startTime = System.currentTimeMillis()
                val source = standardDataSource

                val interval = 1000L

                // Get Traffic Data
                val trafficData = source.getTrafficData()

                if (trafficData != null) {
                    val currentTime = System.currentTimeMillis()
                    val totalRxBytes = trafficData.rxBytes
                    val totalTxBytes = trafficData.txBytes

                    if (lastTime != 0L) {
                        val timeDelta = currentTime - lastTime
                        val rxDelta = totalRxBytes - lastTotalRxBytes
                        val txDelta = totalTxBytes - lastTotalTxBytes

                        if (timeDelta > 0) {
                            // Calculate speed
                            val downloadSpeed = ((rxDelta * 1000) / timeDelta).coerceAtLeast(0)
                            val uploadSpeed = ((txDelta * 1000) / timeDelta).coerceAtLeast(0)

                            if (downloadSpeed != 0L || uploadSpeed != 0L) {
                                _netSpeed.value = NetSpeedData(
                                    downloadSpeed.coerceAtLeast(0),
                                    uploadSpeed.coerceAtLeast(0)
                                )
                            }
                        }
                    }

                    lastTotalRxBytes = totalRxBytes
                    lastTotalTxBytes = totalTxBytes
                    lastTime = currentTime
                }

                // Delay to achieve the desired interval
                val delayMills = interval - (System.currentTimeMillis() - startTime)
                delay(delayMills.coerceAtLeast(0))
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
    }

    companion object {
        fun formatSpeedTextForLiveUpdate(bytes: Long): String {
            if (bytes < 1024) return "${bytes}B/s"
            val kb = bytes / 1024.0
            if (kb < 1000) return "${"%.0f".format(Locale.US, kb)}K/s"
            val mb = kb / 1024.0
            if (mb < 1000) {
                return if (mb < 100) "${"%.1f".format(Locale.US, mb)}M/s"
                else "${"%.0f".format(Locale.US, mb)}M/s"
            }
            val gb = mb / 1024.0
            return "${"%.1f".format(Locale.US, gb)}G/s"
        }

        fun formatSpeedText(bytes: Long): Pair<String, String> {
            if (bytes < 1024) return bytes.toString() to "B/s"
            val kb = bytes / 1024.0
            if (kb < 1000) return "%.0f".format(Locale.US, kb) to "KB/s"
            val mb = kb / 1024.0
            if (mb < 1000) {
                return if (mb < 10) "%.1f".format(Locale.US, mb) to "MB/s"
                else "%.0f".format(Locale.US, mb) to "MB/s"
            }
            val gb = mb / 1024.0
            return "%.1f".format(Locale.US, gb) to "GB/s"
        }

        fun formatSpeedLine(bytes: Long): String {
            val (v, u) = formatSpeedText(bytes)
            return "$v$u"
        }
    }
}
