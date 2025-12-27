package vip.mystery0.pixelpulse.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import vip.mystery0.pixelpulse.ui.overlay.OverlayWindow

class NetworkMonitorService : Service() {
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
        val initialNotif = notificationHelper.buildNotification(NetSpeedData(0, 0), false)

        try {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                initialNotif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
                    if (repository.isOverlayEnabled.value) {
                        overlayWindow.show()
                        overlayWindow.update(speed)
                    } else {
                        overlayWindow.hide()
                    }
                }

                // Notification logic
                val isLiveUpdate = repository.isLiveUpdateEnabled.value
                val notification = notificationHelper.buildNotification(speed, isLiveUpdate)
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
