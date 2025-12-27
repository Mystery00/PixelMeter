package vip.mystery0.pixelpulse.service

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
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import vip.mystery0.pixelpulse.MainActivity
import vip.mystery0.pixelpulse.R
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import java.util.Locale
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

    fun buildNotification(speed: NetSpeedData, isLiveUpdate: Boolean): Notification {
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

        val notification: Notification
        if (isLiveUpdate) {
            // Use static icon (Notification icon shouldn't show speed in this mode)
            val statusText = formatSpeedTextForLiveUpdate(speed.totalSpeed)

            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Network Speed")
                .setContentText(
                    "▼ ${formatSpeedLine(speed.downloadSpeed)}  ▲ ${
                        formatSpeedLine(
                            speed.uploadSpeed
                        )
                    }"
                )
                .setSmallIcon(R.drawable.ic_speed)
                .setShortCriticalText(statusText)
                .setRequestPromotedOngoing(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            // Standard Mode
            val (valueStr, unitStr) = formatSpeedText(speed.totalSpeed)
            // Draw Bitmap with speed
            bitmap.eraseColor(Color.TRANSPARENT)
            val cx = size / 2f
            val cyValue = size * 0.5f
            val cyUnit = size * 0.95f

            canvas.drawText(valueStr, cx, cyValue, textPaint)
            canvas.drawText(unitStr, cx, cyUnit, unitPaint)

            val smallIcon = Icon.createWithBitmap(bitmap)

            notification = Notification.Builder(context, CHANNEL_ID)
                .setContentTitle("Network Speed")
                .setContentText(
                    "RX ${formatSpeedLine(speed.downloadSpeed)}  TX ${
                        formatSpeedLine(
                            speed.uploadSpeed
                        )
                    }"
                )
                .setSmallIcon(smallIcon)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        }
        return notification
    }

    private fun formatSpeedTextForLiveUpdate(bytes: Long): String {
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

    private fun formatSpeedText(bytes: Long): Pair<String, String> {
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

    private fun formatSpeedLine(bytes: Long): String {
        val (v, u) = formatSpeedText(bytes)
        return "$v$u"
    }
}
