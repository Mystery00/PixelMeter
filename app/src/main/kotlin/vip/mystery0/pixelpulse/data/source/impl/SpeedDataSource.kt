package vip.mystery0.pixelpulse.data.source.impl

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.TrafficStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.pixelpulse.data.source.ISpeedDataSource
import vip.mystery0.pixelpulse.data.source.NetworkTrafficData

class SpeedDataSource(
    private val connectivityManager: ConnectivityManager
) : ISpeedDataSource {
    override suspend fun getTrafficData(): NetworkTrafficData? = withContext(Dispatchers.IO) {
        var totalRx = 0L
        var totalTx = 0L

        // 获取当前系统所有已连接的网络句柄
        // 注意：在开启 VPN 时，这里通常会包含两个网络：
        // 1. 物理网络 (如 Wi-Fi)
        // 2. 虚拟网络 (VPN)
        val allNetworks: Array<Network> = connectivityManager.allNetworks

        for (network in allNetworks) {
            val caps = connectivityManager.getNetworkCapabilities(network) ?: continue

            // 核心过滤逻辑：
            // 我们只关心物理接口 (Wi-Fi, 蜂窝, 以太网)
            // 显式忽略 TRANSPORT_VPN，这样就彻底避开了 tun0 等虚拟接口的重复计数
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                continue
            }

            // 检查是否是我们需要统计的物理链路类型
            val isPhysical = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

            if (isPhysical) {
                // 获取该网络的链路属性 (LinkProperties)
                // 这里面包含了对应的接口名，例如 "wlan0", "rmnet_data0", "eth0"
                val linkProps = connectivityManager.getLinkProperties(network)
                val ifaceName = linkProps?.interfaceName

                if (!ifaceName.isNullOrEmpty()) {
                    // API 31+ 专属方法：直接读取指定接口的计数器
                    val rx = TrafficStats.getRxBytes(ifaceName)
                    val tx = TrafficStats.getTxBytes(ifaceName)

                    // TrafficStats.UNSUPPORTED 值为 -1，必须处理
                    if (rx != TrafficStats.UNSUPPORTED.toLong()) {
                        totalRx += rx
                    }
                    if (tx != TrafficStats.UNSUPPORTED.toLong()) {
                        totalTx += tx
                    }
                }
            }
        }

        if (totalRx == 0L && totalTx == 0L) return@withContext null
        return@withContext NetworkTrafficData(totalRx, totalTx)
    }
}
