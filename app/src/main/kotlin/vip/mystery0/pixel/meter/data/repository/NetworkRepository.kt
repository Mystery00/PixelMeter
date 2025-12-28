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
import kotlinx.coroutines.withContext
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

    private val _samplingInterval = MutableStateFlow(1500L)
    val samplingInterval: StateFlow<Long> = _samplingInterval.asStateFlow()

    private val _overlayBgColor = MutableStateFlow(0xCC000000.toInt())
    val overlayBgColor: StateFlow<Int> = _overlayBgColor.asStateFlow()

    private val _overlayTextColor = MutableStateFlow(0xFFFFFFFF.toInt())
    val overlayTextColor: StateFlow<Int> = _overlayTextColor.asStateFlow()

    private val _overlayCornerRadius = MutableStateFlow(8)
    val overlayCornerRadius: StateFlow<Int> = _overlayCornerRadius.asStateFlow()

    private val _overlayTextSize = MutableStateFlow(10f)
    val overlayTextSize: StateFlow<Float> = _overlayTextSize.asStateFlow()

    private val _overlayTextUp = MutableStateFlow("▲ ")
    val overlayTextUp: StateFlow<String> = _overlayTextUp.asStateFlow()

    private val _overlayTextDown = MutableStateFlow("▼ ")
    val overlayTextDown: StateFlow<String> = _overlayTextDown.asStateFlow()

    private val _overlayOrderUpFirst = MutableStateFlow(true)
    val overlayOrderUpFirst: StateFlow<Boolean> = _overlayOrderUpFirst.asStateFlow()

    private val _notificationTextUp = MutableStateFlow("▲ ")
    val notificationTextUp: StateFlow<String> = _notificationTextUp.asStateFlow()

    private val _notificationTextDown = MutableStateFlow("▼ ")
    val notificationTextDown: StateFlow<String> = _notificationTextDown.asStateFlow()

    private val _notificationOrderUpFirst = MutableStateFlow(true)
    val notificationOrderUpFirst: StateFlow<Boolean> = _notificationOrderUpFirst.asStateFlow()

    private val _notificationDisplayMode = MutableStateFlow(0)
    val notificationDisplayMode: StateFlow<Int> = _notificationDisplayMode.asStateFlow()

    private val _notificationTextSize = MutableStateFlow(0.65f)
    val notificationTextSize: StateFlow<Float> = _notificationTextSize.asStateFlow()

    private val _notificationUnitSize = MutableStateFlow(0.35f)
    val notificationUnitSize: StateFlow<Float> = _notificationUnitSize.asStateFlow()

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
            _samplingInterval.value = dataStoreRepository.samplingInterval.first()
            _overlayBgColor.value = dataStoreRepository.overlayBgColor.first()
            _overlayTextColor.value = dataStoreRepository.overlayTextColor.first()
            _overlayCornerRadius.value = dataStoreRepository.overlayCornerRadius.first()
            _overlayTextSize.value = dataStoreRepository.overlayTextSize.first()
            _overlayTextUp.value = dataStoreRepository.overlayTextUp.first()
            _overlayTextDown.value = dataStoreRepository.overlayTextDown.first()
            _overlayOrderUpFirst.value = dataStoreRepository.overlayOrderUpFirst.first()
            _notificationTextUp.value = dataStoreRepository.notificationTextUp.first()
            _notificationTextDown.value = dataStoreRepository.notificationTextDown.first()
            _notificationOrderUpFirst.value = dataStoreRepository.notificationOrderUpFirst.first()
            _notificationDisplayMode.value = dataStoreRepository.notificationDisplayMode.first()
            _notificationTextSize.value = dataStoreRepository.notificationTextSize.first()
            _notificationUnitSize.value = dataStoreRepository.notificationUnitSize.first()
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
        scope.launch {
            dataStoreRepository.samplingInterval.collect { _samplingInterval.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayBgColor.collect { _overlayBgColor.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayTextColor.collect { _overlayTextColor.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayCornerRadius.collect { _overlayCornerRadius.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayTextSize.collect { _overlayTextSize.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayTextUp.collect { _overlayTextUp.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayTextDown.collect { _overlayTextDown.value = it }
        }
        scope.launch {
            dataStoreRepository.overlayOrderUpFirst.collect { _overlayOrderUpFirst.value = it }
        }
        scope.launch {
            dataStoreRepository.notificationTextUp.collect { _notificationTextUp.value = it }
        }
        scope.launch {
            dataStoreRepository.notificationTextDown.collect { _notificationTextDown.value = it }
        }
        scope.launch {
            dataStoreRepository.notificationOrderUpFirst.collect {
                _notificationOrderUpFirst.value = it
            }
        }
        scope.launch {
            dataStoreRepository.notificationDisplayMode.collect {
                _notificationDisplayMode.value = it
            }
        }
        scope.launch {
            dataStoreRepository.notificationTextSize.collect {
                _notificationTextSize.value = it
            }
        }
        scope.launch {
            dataStoreRepository.notificationUnitSize.collect {
                _notificationUnitSize.value = it
            }
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

    fun setSamplingInterval(interval: Long) {
        scope.launch { dataStoreRepository.setSamplingInterval(interval) }
    }

    fun setOverlayBgColor(color: Int) {
        scope.launch { dataStoreRepository.setOverlayBgColor(color) }
    }

    fun setOverlayTextColor(color: Int) {
        scope.launch { dataStoreRepository.setOverlayTextColor(color) }
    }

    fun setOverlayCornerRadius(radius: Int) {
        scope.launch { dataStoreRepository.setOverlayCornerRadius(radius) }
    }

    fun setOverlayTextSize(size: Float) {
        scope.launch { dataStoreRepository.setOverlayTextSize(size) }
    }

    fun setOverlayTextUp(text: String) {
        scope.launch { dataStoreRepository.setOverlayTextUp(text) }
    }

    fun setOverlayTextDown(text: String) {
        scope.launch { dataStoreRepository.setOverlayTextDown(text) }
    }

    fun setOverlayOrderUpFirst(upFirst: Boolean) {
        scope.launch { dataStoreRepository.setOverlayOrderUpFirst(upFirst) }
    }

    fun setNotificationTextUp(text: String) {
        scope.launch { dataStoreRepository.setNotificationTextUp(text) }
    }

    fun setNotificationTextDown(text: String) {
        scope.launch { dataStoreRepository.setNotificationTextDown(text) }
    }

    fun setNotificationOrderUpFirst(upFirst: Boolean) {
        scope.launch { dataStoreRepository.setNotificationOrderUpFirst(upFirst) }
    }

    fun setNotificationDisplayMode(mode: Int) {
        scope.launch { dataStoreRepository.setNotificationDisplayMode(mode) }
    }

    fun setNotificationTextSize(size: Float) {
        scope.launch { dataStoreRepository.setNotificationTextSize(size) }
    }

    fun setNotificationUnitSize(size: Float) {
        scope.launch { dataStoreRepository.setNotificationUnitSize(size) }
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
                val interval = _samplingInterval.value
                val startTime = System.currentTimeMillis()

                // Get Traffic Data
                val trafficData = dataSource.getTrafficData()

                val currentTime = System.currentTimeMillis()
                val totalRxBytes = trafficData.rxBytes
                val totalTxBytes = trafficData.txBytes

                withContext(Dispatchers.Default) {
                    if (lastTime != 0L) {
                        val timeDelta = currentTime - lastTime
                        val rxDelta = totalRxBytes - lastTotalRxBytes
                        val txDelta = totalTxBytes - lastTotalTxBytes

                        if (timeDelta > 0) {
                            // Calculate speed
                            val downloadSpeed = ((rxDelta * 1000) / timeDelta).coerceAtLeast(0)
                            val uploadSpeed = ((txDelta * 1000) / timeDelta).coerceAtLeast(0)

                            _netSpeed.value = NetSpeedData(
                                downloadSpeed.coerceAtLeast(0),
                                uploadSpeed.coerceAtLeast(0)
                            )
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
            if (kb < 1000) return "${"%.0f".format(Locale.getDefault(), kb)}K/s"
            val mb = kb / 1024.0
            if (mb < 1000) {
                return if (mb < 100) "${"%.1f".format(Locale.getDefault(), mb)}M/s"
                else "${"%.0f".format(Locale.getDefault(), mb)}M/s"
            }
            val gb = mb / 1024.0
            return "${"%.1f".format(Locale.getDefault(), gb)}G/s"
        }

        fun formatSpeedText(bytes: Long): Pair<String, String> {
            if (bytes < 1024) return bytes.toString() to "B/s"
            val kb = bytes / 1024.0
            if (kb < 1000) return "%.0f".format(Locale.getDefault(), kb) to "KB/s"
            val mb = kb / 1024.0
            if (mb < 1000) {
                return if (mb < 10) "%.1f".format(Locale.getDefault(), mb) to "MB/s"
                else "%.0f".format(Locale.getDefault(), mb) to "MB/s"
            }
            val gb = mb / 1024.0
            return "%.1f".format(Locale.getDefault(), gb) to "GB/s"
        }

        fun formatSpeedLine(bytes: Long): String {
            val (v, u) = formatSpeedText(bytes)
            return "$v$u"
        }
    }
}
