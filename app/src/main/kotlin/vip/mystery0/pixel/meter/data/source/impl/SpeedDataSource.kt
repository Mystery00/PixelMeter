package vip.mystery0.pixel.meter.data.source.impl

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import vip.mystery0.pixel.meter.data.source.ISpeedDataSource
import vip.mystery0.pixel.meter.data.source.NetworkTrafficData
import java.util.concurrent.ConcurrentHashMap

class SpeedDataSource(
    private val connectivityManager: ConnectivityManager
) : ISpeedDataSource {
    private val validInterfaces = ConcurrentHashMap<Network, String>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetwork(
                network,
                networkCapabilities,
                connectivityManager.getLinkProperties(network)
            )
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            updateNetwork(
                network,
                connectivityManager.getNetworkCapabilities(network),
                linkProperties
            )
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            validInterfaces.remove(network)
        }
    }

    init {
        // 监听所有网络请求，但通过 updateNetwork 过滤
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun updateNetwork(
        network: Network,
        caps: NetworkCapabilities?,
        props: LinkProperties?
    ) {
        if (caps == null || props == null) {
            validInterfaces.remove(network)
            return
        }

        // 核心过滤逻辑：
        // 显式忽略 TRANSPORT_VPN，这样就彻底避开了 tun0 等虚拟接口的重复计数
        val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        if (isVpn) {
            validInterfaces.remove(network)
            return
        }

        // 检查是否是我们需要统计的物理链路类型 (虽然 Request 已经过滤了 Transport，但在 Callback若更新Caps时仍需双重确认)
        val isPhysical = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        if (isPhysical) {
            val ifaceName = props.interfaceName
            if (!ifaceName.isNullOrEmpty()) {
                validInterfaces[network] = ifaceName
            }
        } else {
            validInterfaces.remove(network)
        }
    }

    override suspend fun getTrafficData(): NetworkTrafficData = withContext(Dispatchers.Default) {
        var totalRx = 0L
        var totalTx = 0L

        // 直接遍历缓存的接口名称，无需 IPC 调用查询 Capabilities
        // 使用协程并行获取数据（虽然现在只是读取 TrafficStats，并行IO仍然有微小优势，且保持结构一致）
        val trafficDataList = validInterfaces.values.map { ifaceName ->
            async {
                var currentRx = 0L
                var currentTx = 0L

                // API 31+ 专属方法：直接读取指定接口的计数器
                val rx = withContext(Dispatchers.IO) { TrafficStats.getRxBytes(ifaceName) }
                val tx = withContext(Dispatchers.IO) { TrafficStats.getTxBytes(ifaceName) }

                // TrafficStats.UNSUPPORTED 值为 -1，必须处理
                if (rx != TrafficStats.UNSUPPORTED.toLong()) {
                    currentRx += rx
                }
                if (tx != TrafficStats.UNSUPPORTED.toLong()) {
                    currentTx += tx
                }
                NetworkTrafficData(currentRx, currentTx)
            }
        }.awaitAll()

        trafficDataList.forEach {
            totalRx += it.rxBytes
            totalTx += it.txBytes
        }

        return@withContext NetworkTrafficData(totalRx, totalTx)
    }
}
