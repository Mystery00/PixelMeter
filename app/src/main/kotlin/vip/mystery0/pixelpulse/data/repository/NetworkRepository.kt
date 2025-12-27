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

class NetworkRepository(
    private val standardDataSource: SpeedDataSource,
    private val prefs: SharedPreferences
) : KoinComponent {

    private val _isOverlayEnabled = MutableStateFlow(false)
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isLiveUpdateEnabled = MutableStateFlow(false)
    val isLiveUpdateEnabled: StateFlow<Boolean> = _isLiveUpdateEnabled.asStateFlow()

    private val _netSpeed = MutableStateFlow(NetSpeedData(0, 0))
    val netSpeed: StateFlow<NetSpeedData> = _netSpeed.asStateFlow()

    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L

    init {
        // Initialize Live Update settings
        _isLiveUpdateEnabled.value = prefs.getBoolean("key_live_update", false)
    }

    fun setOverlayEnabled(enable: Boolean) {
        _isOverlayEnabled.value = enable
    }

    fun setLiveUpdateEnabled(enable: Boolean) {
        _isLiveUpdateEnabled.value = enable
        prefs.edit { putBoolean("key_live_update", enable) }
    }

    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        // Reset state
        lastTotalRxBytes = 0L
        lastTotalTxBytes = 0L
        lastTime = 0L

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
    }
}
