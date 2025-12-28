package vip.mystery0.pixel.meter.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import vip.mystery0.pixel.meter.data.source.NetSpeedData
import vip.mystery0.pixel.meter.data.source.impl.SpeedDataSource
import java.util.Locale

class NetworkRepository(
    private val dataSource: SpeedDataSource,
    private val dataStoreRepository: DataStoreRepository,
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
        runBlocking {
            _isLiveUpdateEnabled.value = dataStoreRepository.isLiveUpdateEnabled.first()
            _isNotificationEnabled.value = dataStoreRepository.isNotificationEnabled.first()
            _isOverlayLocked.value = dataStoreRepository.isOverlayLocked.first()
            _isOverlayEnabled.value = dataStoreRepository.isOverlayEnabled.first()
        }
        scope.launch {
            dataStoreRepository.isLiveUpdateEnabled.collect { _isLiveUpdateEnabled.value = it }
        }
        scope.launch {
            dataStoreRepository.isNotificationEnabled.collect { _isNotificationEnabled.value = it }
        }
        scope.launch {
            dataStoreRepository.isOverlayLocked.collect { _isOverlayLocked.value = it }
        }
        scope.launch {
            dataStoreRepository.isOverlayEnabled.collect { _isOverlayEnabled.value = it }
        }
    }

    fun setOverlayEnabled(enable: Boolean) {
        scope.launch { dataStoreRepository.setOverlayEnabled(enable) }
    }

    fun setLiveUpdateEnabled(enable: Boolean) {
        scope.launch { dataStoreRepository.setLiveUpdateEnabled(enable) }
    }

    fun setNotificationEnabled(enable: Boolean) {
        scope.launch { dataStoreRepository.setNotificationEnabled(enable) }
    }

    fun setOverlayLocked(locked: Boolean) {
        scope.launch { dataStoreRepository.setOverlayLocked(locked) }
    }

    suspend fun getOverlayPosition(): Pair<Int, Int> {
        val x = dataStoreRepository.overlayX.first()
        val y = dataStoreRepository.overlayY.first()
        return x to y
    }

    fun saveOverlayPosition(x: Int, y: Int) {
        scope.launch {
            dataStoreRepository.saveOverlayPosition(x, y)
        }
    }

    fun startMonitoring() {
        Log.i(TAG, "request start monitoring")
        if (monitoringJob?.isActive == true) return

        // Reset state
        lastTotalRxBytes = 0L
        lastTotalTxBytes = 0L
        lastTime = 0L

        _isMonitoring.value = true

        monitoringJob = scope.launch {
            Log.i(TAG, "startMonitoring")
            while (isActive) {
                val startTime = System.currentTimeMillis()
                val source = dataSource

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
        Log.i(TAG, "request stop monitoring")
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
        _netSpeed.value = NetSpeedData(0, 0)
    }

    companion object {
        private const val TAG = "NetworkRepository"

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
