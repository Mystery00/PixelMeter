# 核心功能需求 (Product Requirements)

| 项目       | 内容                                          |
|----------|---------------------------------------------|
| **产品名称** | PixelMeter                                  |
| **版本**   | v0.1.0                                      |
| **定位**   | 专为 Pixel/原生 Android 设计的精准网速指示器（智能剔除 VPN 流量） |

## 1. 项目背景与痛点

在开启 VPN（如 V2Ray, Clash, WireGuard 等）的环境下，Android 传统的网速显示 App (基于 `TrafficStats`/
`NetworkStatsManager`) 往往会将物理接口（`wlan0`/`rmnet`）与虚拟接口（`tun0`
）的流量叠加计算。这导致通知栏显示的网速通常是实际速度的 **2 倍**，产生误导。

**PixelMeter** 旨在通过 **单一数据源策略** 解决此痛点，利用 `ConnectivityManager` 智能识别物理接口，直接读取
Kernel 数据剔除虚拟接口流量，还原真实网速。

## 2. 核心特性 (Features)

### 2.1 精准流量统计 (Native)

* **核心机制**: 使用 Android 原生 `TrafficStats` 配合 `ConnectivityManager`。
* **智能过滤**:
  * 自动识别所有物理接口 (Wi-Fi, Cellular, Ethernet)。
  * **自动排除** VPN (`tun0` 等) 虚拟接口，无需用户手动配置黑名单。
  * **计算公式**: `TrueSpeed = Sum(Physical_Interfaces_Rx/Tx)`。

### 2.2 原生体验 (Native Experience)

* **Design**: Material 3 + Material You + **Material Express** 风格。
* **Android 14/15 适配**: 针对最新的 Android API 进行优化。
* **无广告/轻量级**: 专注于核心功能，极低功耗。

### 2.3 多样的显示方式

* **首次启动**: 默认**不开启**任何显示，由用户自行选择通知栏或悬浮窗。
* **通知栏动态图标**: 实时绘制 Bitmap 更新通知栏图标。
* **桌面悬浮窗**: 支持独立开关与拖拽。
* **同时显示模式 (Both)**:
    * 显示逻辑: `Total Speed = Upload + Download`。
    * UI 表现: 仅展示一行合并后的总网速文本。

### 2.4 实用工具箱

* **Cloudflare 测速**: 集成 Chrome Custom Tabs (CCT) 快速访问 `speed.cloudflare.com`。

## 3. 技术规格 (Technical Requirements)

* **Min SDK**: 36 (Android 16)
* **Target SDK**: 36 (Android 16)
* **架构**: MVVM + Clean Architecture (Simplified)
* **语言**: Kotlin
* **UI**: Jetpack Compose
* **DI**: Koin

## 4. 功能需求详情

| ID      | 模块       | 功能点    | 描述                                                       | 优先级 |
|:--------|:---------|:-------|:---------------------------------------------------------|:----|
| **F01** | **核心服务** | 前台服务保活 | 启动 `dataSync` 类型的 Foreground Service，需处理 Android 14+ 适配。 | P0  |
| **F02** | **数据源**  | 核心数据源  | 调用 `ConnectivityManager` + `TrafficStats` 获取过滤后的物理接口流量。  | P0  |
| **F04** | **UI**   | 仪表盘首页  | 显示当前网速、服务运行状态。                                           | P0  |

| **F06** | **UI**   | 通知栏更新       | 每秒绘制 Bitmap 并更新 Notification。                            | P0  |
| **F07** | **UI**   | 悬浮窗         | 实现 Compose 悬浮窗，处理 Touch 事件与 WindowManager 交互。            | P1  |
| **F08** | **工具**   | 网络测速        | CCT 呼起 Cloudflare Speed Test。                            | P2  |

## 5. 数据存储 (DataStore)

| Key                        | 类型      | 默认值     | 说明        |
|:---------------------------|:--------|:--------|:----------|
| `key_live_update`          | Boolean | `false` | 首页实时更新开关  |
| `key_notification_enabled` | Boolean | `true`  | 通知栏开关     |
| `key_overlay_enabled`      | Boolean | `false` | 悬浮窗开关     |
| `key_overlay_locked`       | Boolean | `false` | 悬浮窗位置锁定开关 |
| `key_overlay_x`            | Int     | `100`   | 悬浮窗 X 坐标  |
| `key_overlay_y`            | Int     | `200`   | 悬浮窗 Y 坐标  |

## 6. 非功能性需求 (NFR)

* **功耗控制**: 屏幕关闭时应停止或降低刷新频率（待定，需权衡后台保活需求）。
* **隐私安全**: 仅在本地处理流量计数，绝不上传任何网络流量数据。
