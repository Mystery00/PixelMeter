# Pixel Meter

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="200" alt="Pixel Meter Logo"/>
</p>

<p align="center">
  <strong>专为 Pixel 和原生 Android 设计的精准网速指示器。</strong>
</p>

<p align="center">
    <a href="https://github.com/Mystery00/PixelMeter/releases"><img src="https://img.shields.io/github/v/release/Mystery00/PixelMeter" alt="GitHub Release"></a>
    <a href="https://play.google.com/store/apps/details?id=vip.mystery0.pixel.meter"><img src="https://img.shields.io/badge/Google_Play-PixelMeter-green?logo=google-play&logoColor=white" alt="Google Play"></a>
    <a href="LICENSE"><img src="https://img.shields.io/github/license/Mystery00/PixelMeter" alt="License"></a>
</p>

[English](README.md)

## 简介

Pixel Meter 是一款专为 Google Pixel 和原生 Android 设备设计的网速监控应用。它可以解决在使用 VPN
时，传统网速显示应用因同时统计物理接口和虚拟接口流量而导致显示速度翻倍的问题。

**Pixel Meter 智能过滤 VPN 流量**，直接从物理网络接口（Wi-Fi、蜂窝网络、以太网）读取数据，提供精准的实时网速统计。

## 软件截图

<p align="center">
  <img src="docs/Screenshot_CN.png" width="400" alt="软件截图"/>
</p>
<p align="center">
  <img src="docs/Component.png" width="175" alt="软件组件展示"/>
</p>

## 核心功能

- **精准流量统计**: 结合 `ConnectivityManager` 和 `TrafficStats`，自动剔除 `tun0` 等虚拟接口流量。
- **原生体验**: 基于 Jetpack Compose 和 Material 3 构建，支持 Pixel 动态取色。
- **多种显示方式**:
    - **通知栏**: 实时绘制动态图标。
    - **悬浮窗**: 支持独立开关和拖拽。
    - **合并模式**: 支持显示总网速（上传+下载）。
- **隐私安全**: 所有数据仅在本地处理，绝不上传任何流量数据。
- **内置工具**: 集成 Chrome Custom Tabs，可快速访问 Cloudflare 进行测速。

## 系统要求

- **设备**: 推荐 Google Pixel 系列，或运行原生 Android (AOSP) 的设备。
- **Android 版本**: Android 12 (API Level 31) 及以上。
- **权限**: 通知权限（用于状态栏图标）和悬浮窗权限（用于桌面悬浮窗）。

## 技术架构

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material 3)
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Koin
- **核心数据源**: `TrafficStats` + `ConnectivityManager` (单一可信数据源)

## 许可证

本项目采用 Apache License 2.0 许可证。详情请参阅 [LICENSE](LICENSE) 文件。
