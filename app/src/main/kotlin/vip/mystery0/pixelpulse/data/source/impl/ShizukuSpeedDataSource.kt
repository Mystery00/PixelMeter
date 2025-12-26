package vip.mystery0.pixelpulse.data.source.impl

import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import vip.mystery0.pixelpulse.data.source.ISpeedDataSource
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import java.lang.reflect.Field
import java.lang.reflect.Method

class ShizukuSpeedDataSource(
    initialBlacklist: Set<String> = emptySet()
) : ISpeedDataSource {

    private var blacklistedInterfaces: Set<String> = initialBlacklist
    private var service: Any? = null
    private var getNetworkStatsSummaryDevMethod: Method? = null

    // Reflection cache
    private var statsClass: Class<*>? = null
    private var ifaceField: Field? = null
    private var rxBytesField: Field? = null
    private var txBytesField: Field? = null
    private var sizeField: Field? = null

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L

    init {
        try {
            HiddenApiBypass.addHiddenApiExemptions("")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        initService()
    }

    fun updateBlacklist(list: Set<String>) {
        blacklistedInterfaces = list
    }

    private fun initService() {
        if (service != null) return
        if (!Shizuku.pingBinder()) return

        try {
            val binder =
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("network_management"))
            val stubClass = Class.forName("android.os.INetworkManagementService\$Stub")
            val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
            service = asInterfaceMethod.invoke(null, binder)

            val serviceClass = service!!.javaClass
            getNetworkStatsSummaryDevMethod = serviceClass.getMethod("getNetworkStatsSummaryDev")
        } catch (e: Exception) {
            e.printStackTrace()
            service = null
        }
    }

    override suspend fun getNetSpeed(): NetSpeedData = withContext(Dispatchers.IO) {
        if (service == null) {
            initService()
            // If still null, return 0
            if (service == null) return@withContext NetSpeedData(0, 0)
        }

        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastTime

        if (timeDelta <= 0 && lastTime != 0L) return@withContext NetSpeedData(0, 0)

        val (totalRx, totalTx) = getStatsFromBinder()

        var downloadSpeed = 0L
        var uploadSpeed = 0L

        if (lastTime != 0L) {
            val rxDelta = totalRx - lastTotalRxBytes
            val txDelta = totalTx - lastTotalTxBytes
            if (timeDelta > 0) {
                downloadSpeed = (rxDelta * 1000) / timeDelta
                uploadSpeed = (txDelta * 1000) / timeDelta
            }
        }

        lastTotalRxBytes = totalRx
        lastTotalTxBytes = totalTx
        lastTime = currentTime

        return@withContext NetSpeedData(
            downloadSpeed.coerceAtLeast(0),
            uploadSpeed.coerceAtLeast(0)
        )
    }

    private fun getStatsFromBinder(): Pair<Long, Long> {
        try {
            val statsObj = getNetworkStatsSummaryDevMethod?.invoke(service) ?: return 0L to 0L

            if (statsClass == null) {
                statsClass = statsObj.javaClass
                ifaceField = statsClass!!.getDeclaredField("iface").apply { isAccessible = true }
                rxBytesField =
                    statsClass!!.getDeclaredField("rxBytes").apply { isAccessible = true }
                txBytesField =
                    statsClass!!.getDeclaredField("txBytes").apply { isAccessible = true }
                try {
                    sizeField = statsClass!!.getDeclaredField("size").apply { isAccessible = true }
                } catch (e: NoSuchFieldException) {
                    // Start from Android 12?, NetworkStats might not have size field but rely on array length
                }
            }

            val ifaces = ifaceField!!.get(statsObj) as Array<String?>
            val rxs = rxBytesField!!.get(statsObj) as LongArray
            val txs = txBytesField!!.get(statsObj) as LongArray

            // Determine size
            val size = sizeField?.getInt(statsObj) ?: ifaces.size

            var totalRx = 0L
            var totalTx = 0L

            for (i in 0 until size) {
                val iface = ifaces[i]
                if (iface == null || iface == "lo" || blacklistedInterfaces.contains(iface)) continue
                totalRx += rxs[i]
                totalTx += txs[i]
            }

            return totalRx to totalTx

        } catch (e: Exception) {
            e.printStackTrace()
            return 0L to 0L
        }
    }
}
