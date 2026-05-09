# 隐私政策

**生效日期:** 2025-12-27

Pixel Meter（以下简称“我们”）致力于保护您的隐私。本隐私政策旨在向您说明我们的应用如何处理您的信息。

## 1. 信息收集与使用

**我们要明确声明：我们不会收集、存储或传输任何个人数据或设备数据。**

Pixel Meter 的所有网速监控逻辑均在您的设备上本地运行（“本地处理”）。我们不会将任何关于您的网络使用情况、访问的网站或应用使用情况的数据发送至我们的服务器或任何第三方。

## 2. 权限说明

为了提供核心功能，Pixel Meter 需要申请以下权限。这些权限仅用于下述目的：

* **`android.permission.ACCESS_NETWORK_STATE`**: 用于检测网络连接状态，并识别物理网络接口（如
  Wi-Fi、蜂窝网络）与虚拟接口（如 VPN）。
* **`android.permission.INTERNET`**: 仅在您主动点击“网络测速”功能（通过
  Cloudflare）时使用。本应用不会为了后台数据上报而保持持续的联网连接。
* **`android.permission.FOREGROUND_SERVICE`**: 用于确保网速监控服务在后台保持运行，以便实时更新通知栏或悬浮窗的网速读数。
* **`android.permission.SYSTEM_ALERT_WINDOW`**: 仅当您选择开启“悬浮窗”功能时需要，用于在其他应用上层显示网速。
* **`android.permission.POST_NOTIFICATIONS`**: 用于在状态栏或通知中心显示实时的网速信息。

## 3. 第三方服务

Pixel Meter 不包含任何第三方统计、广告 SDK 或追踪工具。

应用内的“网络测速”功能会通过 Chrome Custom Tabs 打开网页 (`speed.cloudflare.com`)。此交互受
Cloudflare 的隐私政策以及您浏览器的隐私设置管辖。

## 4. 儿童隐私

我们的服务不针对 13 岁以下的任何人。我们不会有意收集 13 岁以下儿童的个人身份信息。

## 5. 隐私政策的变更

我们可能会不时更新我们的隐私政策。因此，建议您定期查看本页面以获取任何变更。我们将通过在此页面上发布新的隐私政策来通知您任何更改。

## 6. 联系我们

如果您对我们的隐私政策有任何疑问或建议，请随时通过我们的 [GitHub 项目主页](https://github.com/Pixel-Tailor-CN/PixelMeter)
提交 Issue 与我们联系。
