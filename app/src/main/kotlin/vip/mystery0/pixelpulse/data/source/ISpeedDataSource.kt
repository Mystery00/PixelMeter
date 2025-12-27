package vip.mystery0.pixelpulse.data.source

import androidx.compose.runtime.Immutable

@Immutable
data class NetSpeedData(
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L,
    val totalSpeed: Long = downloadSpeed + uploadSpeed,
)

data class NetworkTrafficData(
    val rxBytes: Long,
    val txBytes: Long,
)

interface ISpeedDataSource {
    suspend fun getTrafficData(): NetworkTrafficData?
}
