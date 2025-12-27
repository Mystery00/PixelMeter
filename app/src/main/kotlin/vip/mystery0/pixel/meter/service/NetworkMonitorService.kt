package vip.mystery0.pixel.meter.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.data.source.NetSpeedData
import vip.mystery0.pixel.meter.ui.overlay.OverlayWindow

class NetworkMonitorService : Service() {
    companion object {
        private const val TAG = "NetworkMonitorService"
    }

    private val repository: NetworkRepository by inject()
    private val notificationHelper by lazy { NotificationHelper(this) }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private val overlayWindow: OverlayWindow by inject()

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotif = notificationHelper.buildNotification(NetSpeedData(0, 0), false, true)

        try {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                initialNotif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand: start foreground error", e)
            stopSelf()
            return START_NOT_STICKY
        }

        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceJob?.cancel()

        // Start Repository Monitoring
        repository.startMonitoring()

        serviceJob = scope.launch {
            repository.netSpeed.collect { speed ->
                // Overlay logic
                withContext(Dispatchers.Main) {
                    try {
                        if (repository.isOverlayEnabled.value) {
                            overlayWindow.show()
                            overlayWindow.update(speed)
                        } else {
                            overlayWindow.hide()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "startMonitoring: overlay window error", e)
                    }
                }

                // Notification logic
                val isLiveUpdate = repository.isLiveUpdateEnabled.value
                val isNotificationEnabled = repository.isNotificationEnabled.value
                val notification =
                    notificationHelper.buildNotification(speed, isLiveUpdate, isNotificationEnabled)
                notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        repository.stopMonitoring()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
