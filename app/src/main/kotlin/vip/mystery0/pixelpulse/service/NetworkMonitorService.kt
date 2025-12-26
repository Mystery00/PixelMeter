package vip.mystery0.pixelpulse.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.data.source.NetSpeedData

class NetworkMonitorService : Service() {

    private val repository: NetworkRepository by inject()
    private val notificationHelper by lazy { NotificationHelper(this) }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val overlayWindow: vip.mystery0.pixelpulse.ui.overlay.OverlayWindow by inject()

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotif = notificationHelper.buildNotification(NetSpeedData(0, 0))

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    initialNotif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID, initialNotif)
            }
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
        serviceJob = scope.launch {
            while (isActive) {
                val startTime = System.currentTimeMillis()

                val speed = repository.getCurrentSpeed()

                // Overlay logic
                // Ensure we run show/hide on Main Thread if it touches views?
                // OverlayWindow uses WindowManager adds view. WindowManager calls should be thread safe or UI thread?
                // ComposeView creation MUST be on Main Thread.
                // So show()/hide() must switch to Main.

                withContext(Dispatchers.Main) {
                    if (repository.isOverlayEnabled.value) {
                        overlayWindow.show()
                        overlayWindow.update(speed)
                    } else {
                        overlayWindow.hide()
                    }
                }

                val notification = notificationHelper.buildNotification(speed)
                notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)

                val elapsed = System.currentTimeMillis() - startTime
                val delayTime = (1000 - elapsed).coerceAtLeast(100)
                delay(delayTime)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        stopForeground(true)
    }
}
