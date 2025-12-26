package vip.mystery0.pixelpulse.data.source.impl

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.pixelpulse.data.source.ISpeedDataSource
import vip.mystery0.pixelpulse.data.source.NetSpeedData

class StandardSpeedDataSource(
    private val context: Context,
    private val networkStatsManager: NetworkStatsManager
) : ISpeedDataSource {

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L

    override suspend fun getNetSpeed(): NetSpeedData = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastTime

        // Prevent division by zero or negative time
        if (timeDelta <= 0 && lastTime != 0L) return@withContext NetSpeedData(0, 0)

        val totalRxBytes = getTotalRxBytes()
        val totalTxBytes = getTotalTxBytes()

        var downloadSpeed = 0L
        var uploadSpeed = 0L

        if (lastTime != 0L) {
            val rxDelta = totalRxBytes - lastTotalRxBytes
            val txDelta = totalTxBytes - lastTotalTxBytes

            // Normalize to bytes per second
            if (timeDelta > 0) {
                downloadSpeed = (rxDelta * 1000) / timeDelta
                uploadSpeed = (txDelta * 1000) / timeDelta
            }
        }

        lastTotalRxBytes = totalRxBytes
        lastTotalTxBytes = totalTxBytes
        lastTime = currentTime

        return@withContext NetSpeedData(
            downloadSpeed.coerceAtLeast(0),
            uploadSpeed.coerceAtLeast(0)
        )
    }

    private fun getTotalRxBytes(): Long {
        return try {
            val wifiStats = networkStatsManager.querySummaryForDevice(
                NetworkCapabilities.TRANSPORT_WIFI,
                null,
                0,
                System.currentTimeMillis()
            )
            val mobileStats = networkStatsManager.querySummaryForDevice(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                0,
                System.currentTimeMillis()
            )
            wifiStats.rxBytes + mobileStats.rxBytes
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun getTotalTxBytes(): Long {
        return try {
            val wifiStats = networkStatsManager.querySummaryForDevice(
                NetworkCapabilities.TRANSPORT_WIFI,
                null,
                0,
                System.currentTimeMillis()
            )
            val mobileStats = networkStatsManager.querySummaryForDevice(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                0,
                System.currentTimeMillis()
            )
            wifiStats.txBytes + mobileStats.txBytes
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

}
