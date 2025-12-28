# 数据源策略 (Data Source Strategy)

## 概述

Pixel Meter 采用**单一数据源模式**，利用 Android 原生 `TrafficStats` API 获取实时网速。
通过指定接口名称 (`wlan0`) 和移动网络接口，我们可以精确统计流量并计算网速，无需 Root 权限，也无需复杂的
Shizuku IPC。

## 核心策略: NetworkCallback 缓存 + TrafficStats

### 原理

为了进一步优化性能并消除多网卡场景下的时间偏差 (Time Skew)，我们采用了**事件驱动的缓存策略**。

1. **接口缓存**: 在 `SpeedDataSource` 初始化时注册 `ConnectivityManager.NetworkCallback`。
2. **实时更新**:
   - **onAvailable / onCapabilitiesChanged**: 检查网络类型。
      - **排除**: `TRANSPORT_VPN` (避免双重统计)。
      - **包含**: `TRANSPORT_WIFI`, `TRANSPORT_CELLULAR`, `TRANSPORT_ETHERNET`。
      - 若符合条件，将 `<Network, InterfaceName>` 存入 `ConcurrentHashMap`。
   - **onLost**: 从缓存中移除对应的 Network。
3. **极速读取**:
   - `getTrafficData()` 不再进行任何 IPC 调用查询 `ConnectivityManager`。
   - 直接遍历缓存中的接口名称列表 (`wlan0`, `rmnet_data0` 等)。
   - 使用协程并行调用 `TrafficStats.getRx/TxBytes(iface)`。

### 代码结构

```kotlin
// 伪代码
private val validInterfaces = ConcurrentHashMap<Network, String>()

// 初始化注册回调
init {
    connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
        override fun onCapabilitiesChanged(network, caps) {
            if (isPhysical(caps) && !isVpn(caps)) {
                validInterfaces[network] = linkProps.interfaceName
            }
        }
        override fun onLost(network) {
            validInterfaces.remove(network)
        }
    })
}

// 瞬时读取 (无 IPC 开销)
override suspend fun getTrafficData() = validInterfaces.values.map { iface ->
    async { TrafficStats.getRxBytes(iface) }
}.awaitAll().sum()
```

### 优势

1. **零 IPC 延迟**: 采样循环中移除了耗时的 `getNetworkCapabilities` 和 `getLinkProperties` 跨进程调用。
2. **高并发精度**: 结合协程并行读取，采样窗口被压缩到极致，消除了“网卡A读在上一秒，网卡B读在下一秒”的交叉统计误差。
3. **系统减负**: 减少了对 SystemServer 的频繁轮询压力。
4. **无需 Root/ADB**: 保持了原有方案的权限优势。

## 历史演进 (已废弃方案)

### 1. NetworkStatsManager (已移除)

* **问题**: 系统级 Bucket 导致 2-3 小时的延迟归档，无法用于实时网速显示（会有 "0 -> 0 -> 巨大脉冲"
  的现象）。
* **尝试优化**: 曾尝试 2秒 采样窗口平滑处理，但体验仍由延迟。

### 2. Shizuku (Binder IPC) (已移除)

* **问题**: 需要用户激活 Shizuku，门槛较高。
* **状态**: 随着 `TrafficStats` 物理接口方案的验证成功，Shizuku 模式已被彻底移除，简化了项目架构。

## 兼容性

* **MinSDK**: 31 (Android 12)。
* **Device**: Google Pixel 系列 (主要目标)，以及其他遵循标准接口命名的 Android 设备。
