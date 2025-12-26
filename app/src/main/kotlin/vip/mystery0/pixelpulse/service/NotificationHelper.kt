package vip.mystery0.pixelpulse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.graphics.createBitmap
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import java.util.Locale
import kotlin.math.roundToInt

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "pixel_pulse_monitor"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        textSize = size * 0.45f // Value text
    }

    private val unitPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT
        textSize = size * 0.3f // Unit text
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Network Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows real-time network speed in status bar"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(speed: NetSpeedData): Notification {
        val totalSpeed = speed.totalSpeed
        val (valueStr, unitStr) = formatSpeedUsage(totalSpeed)

        // Draw Bitmap
        bitmap.eraseColor(Color.TRANSPARENT)
        val cx = size / 2f
        // Draw Value stacked above Unit?
        // Or just Value if it fits?
        // Let's stack: Value roughly top-mid, Unit bottom-mid
        val cyValue = size * 0.45f
        val cyUnit = size * 0.35f

        canvas.drawText(valueStr, cx, cyValue, textPaint)
        canvas.drawText(unitStr, cx, cyUnit, unitPaint)

        val icon = Icon.createWithBitmap(bitmap)

        val builder = Notification.Builder(context, CHANNEL_ID)

        val intent = Intent().apply {
            setClassName(context, "vip.mystery0.pixelpulse.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder
                .setContentTitle("Network Speed")
                .setContentText(
                    "▼ ${formatSpeedLine(speed.downloadSpeed)}  ▲ ${
                        formatSpeedLine(
                            speed.uploadSpeed
                        )
                    }"
                )
                .setSmallIcon(icon)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        } else {
            builder
                .setContentTitle("Network Speed")
                .setContentText(
                    "▼ ${formatSpeedLine(speed.downloadSpeed)}  ▲ ${
                        formatSpeedLine(
                            speed.uploadSpeed
                        )
                    }"
                )
                .setSmallIcon(icon)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build()
        }
    }

    private fun formatSpeedUsage(bytes: Long): Pair<String, String> {
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
        val (v, u) = formatSpeedUsage(bytes)
        return "$v$u/s"
    }
}
