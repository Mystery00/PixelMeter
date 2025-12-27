package vip.mystery0.pixel.meter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import vip.mystery0.pixel.meter.MainActivity
import vip.mystery0.pixel.meter.R
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.data.source.NetSpeedData
import kotlin.math.roundToInt

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "net_monitor"
        const val CHANNEL_NAME = "Network Monitor"
        const val NOTIFICATION_ID = 1001

        fun createNotificationChannel(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val group = NotificationChannelGroup(
                CHANNEL_ID,
                CHANNEL_NAME
            )
            notificationManager.createNotificationChannelGroup(group)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows real-time network speed in status bar"
                setShowBadge(false)
                setGroup(CHANNEL_ID)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    // Icon generation
    // On Pixel, small icon is typically 24dp. We render at higher res (e.g. 48px or 96px) for clarity
    private val size =
        (context.resources.displayMetrics.density * 24).roundToInt().coerceAtLeast(48)
    private val bitmap = createBitmap(size, size)
    private val canvas = Canvas(bitmap)

    // Paints
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textSize = size * 0.65f // Value text
    }

    private val unitPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textSize = size * 0.35f // Unit text
    }

    fun buildNotification(
        speed: NetSpeedData,
        isLiveUpdate: Boolean,
        isNotificationEnabled: Boolean
    ): Notification {
        val intent = Intent().apply {
            setClassName(context, MainActivity::class.java.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Common Builder setup
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (!isNotificationEnabled) {
            // Notification Disabled (Static Mode)
            return builder
                .setContentTitle("Network Speed")
                .setContentText("Monitoring in background...")
                .setSmallIcon(R.drawable.ic_speed)
                .build()
        }

        // Notification Enabled (Dynamic Mode)
        if (isLiveUpdate) {
            // Live Update Mode
            val statusText = NetworkRepository.formatSpeedTextForLiveUpdate(speed.totalSpeed)
            builder
                .setContentTitle("Network Speed")
                .setContentText(
                    "â–?${NetworkRepository.formatSpeedLine(speed.downloadSpeed)}  â–?${
                        NetworkRepository.formatSpeedLine(speed.uploadSpeed)
                    }"
                )
                .setSmallIcon(R.drawable.ic_speed)
                .setShortCriticalText(statusText) // No version check needed as minSdk = 36
                .setRequestPromotedOngoing(true)
        } else {
            // Standard Mode
            val (valueStr, unitStr) = NetworkRepository.formatSpeedText(speed.totalSpeed)

            // Draw Bitmap with speed
            bitmap.eraseColor(Color.TRANSPARENT)
            val cx = size / 2f
            val cyValue = size * 0.5f
            val cyUnit = size * 0.95f

            canvas.drawText(valueStr, cx, cyValue, textPaint)
            canvas.drawText(unitStr, cx, cyUnit, unitPaint)

            val smallIcon = IconCompat.createWithBitmap(bitmap)

            builder
                .setContentTitle("Network Speed")
                .setContentText(
                    "RX ${NetworkRepository.formatSpeedLine(speed.downloadSpeed)}  TX ${
                        NetworkRepository.formatSpeedLine(speed.uploadSpeed)
                    }"
                )
                .setSmallIcon(smallIcon)
        }

        return builder.build()
    }
}
