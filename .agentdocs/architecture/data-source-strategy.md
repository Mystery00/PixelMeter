# 数据源策略 (Data Source Strategy)

## 概述

Pixel Meter 采用**单一数据源模式**，利用 Android 原生 `TrafficStats` API 获取实时网速。
通过指定接口名称 (`wlan0`) 和移动网络接口，我们可以精确统计流量并计算网速，无需 Root 权限，也无需复杂的
Shizuku IPC。

## 核心策略: TrafficStats 差值计算

### 原理

Android 的 `TrafficStats` 提供了直接读取内核网络计数器的能力。
为了避免 VPN 造成的流量双重统计（虚拟接口 + 物理接口），我们**显式只读取物理接口**的数据：

1. **遍历网络**: 使用 `ConnectivityManager.allNetworks` 获取当前活动网络。
2. **能力过滤**: 检查 `NetworkCapabilities`。
    - **排除**: `TRANSPORT_VPN` (核心步骤，避免双重统计)。
    - **包含**: `TRANSPORT_WIFI`, `TRANSPORT_CELLULAR`, `TRANSPORT_ETHERNET`。
3. **接口读取**: 获取 `LinkProperties` 中的接口名 (如 `wlan0`, `rmnet_data0`)。
4. **统计汇总**: 调用 `TrafficStats.getRx/TxBytes(iface)` 累加数据。

```kotlin
// 伪代码示例
connectivityManager.allNetworks.forEach { network ->
    val caps = connectivityManager.getNetworkCapabilities(network) ?: return@forEach
    if (caps.hasTransport(TRANSPORT_VPN)) return@forEach // 忽略 VPN

    if (caps.hasTransport(TRANSPORT_WIFI) || caps.hasTransport(TRANSPORT_CELLULAR)) {
        val iface = connectivityManager.getLinkProperties(network)?.interfaceName
        totalRx += TrafficStats.getRxBytes(iface)
    }
}
```

### 优势

1. **实时性**: 直接读取 `/proc/net/xt_qtaguid/stats` 或 `/sys/class/net/.../statistics` (由
   Framework 封装)，无缓存桶延迟。
2. **准确性**: 物理接口流量真实反映了网卡吞吐量。
3. **简单性**: 无需处理 `NetworkStatsManager` 的 Session、Bucket、SubscriberId 权限等复杂逻辑。
4. **无需 Root/ADB**: 普通应用权限即可运行。

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
