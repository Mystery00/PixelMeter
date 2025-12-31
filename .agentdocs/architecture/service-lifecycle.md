# 服务生命周期与保活 (Service Lifecycle)

为了保证网速指示器在后台持续运行并实时更新，App 必须维护一个稳定的前台服务 (Foreground Service)。

## 1. Service 配置

- **类名**: `NetworkMonitorService`
- **类型**: `Foreground Service`
- **foregroundServiceType**: `dataSync` (Android 14+ 强制要求指定类型并声明权限)。

```xml

<service android:name=".service.NetworkMonitorService" android:foregroundServiceType="dataSync"
    android:exported="false" />
```

## 2. 启动与保活

### 2.1 启动流程

1. **用户开启**: 用户在主界面 Toggle "Enable Monitor"。
2. **Context.startForegroundService()**: 启动服务。
3. **startForeground()**: 服务 onCreate/onStartCommand 中必须在 5 秒内调用 `startForeground`
   ，绑定一个持续显示的 Notification，否则会被系统杀掉并抛出 ANR。
   26:
   27: ### 2.2 开机自启 (Auto-start)
   28:
   29: - **触发机制**: 监听 `BOOT_COMPLETED` 和 `QUICKBOOT_POWERON` 广播。
   30: - **条件判断**:
   31:     1. 用户开关: 检查 `key_auto_start_service` 是否为 `true`。
   32:     2. 权限校验: 直检查应用是否持有 **悬浮窗权限** 或 **通知权限**。若两者皆无，则视为非法状态，不启动服务（避免
   Android 14+ 启动 FGS 崩溃）。
   33: - **实现**: `BootReceiver` 在满足上述条件后调用 `startForegroundService`。

### 2.2 周期性任务 (Ticker)

- 使用 Kotlin Coroutines `flow` 或 `Handler` 实现 1000ms 的周期性任务。
- **任务内容**:
    - 获取当前网速 (Repository.getSpeed)。
    - 生成 Notification Bitmap。
    - 更新 NotificationManager。
    - 发送 EventBus/StateFlow 消息通知 UI 层 (悬浮窗/主页)。

## 3. Android 14 (API 34) 适配

Android 14 对前台服务有严格限制：

- **权限声明**: 必须在 Manifest 中声明 `android.permission.FOREGROUND_SERVICE` 和
  `android.permission.FOREGROUND_SERVICE_DATA_SYNC`。
- **运行时机**: 仅当 App 处于前台（Visible）时才能调用 `startForegroundService`。若 App 在后台尝试启动服务，会抛出
  `ForegroundServiceStartNotAllowedException`。
    - **处理策略**: 确保服务的启动操作仅由用户在 UI 界面手动触发，或者在 BootReceiver (开机自启)
      中依循系统允许的豁免规则进行。

## 4. 资源释放

- 当用户手动关闭功能，或点击通知栏 "Exit" 按钮时，调用 `stopForeground(STOP_FOREGROUND_REMOVE)` 并
  `stopSelf()`，彻底释放资源，停止计费/耗电。

## 5. 电量与性能优化 (Power Optimization)

为了避免长期占用 CPU 导致设备无法休眠，App 内置了智能休眠与唤醒机制：

### 5.1 屏幕状态监听

Service 内部通过 `BroadcastReceiver` 监听屏幕状态广播：

- `Intent.ACTION_SCREEN_OFF`: 屏幕关闭。
- `Intent.ACTION_SCREEN_ON`: 屏幕点亮。

### 5.2 智能休眠策略

1. **延迟停止**: 当检测到 **屏幕关闭** 时，Service 并不会立即停止监听（考虑到用户可能短按电源键），而是启动一个
   **2分钟** 的倒计时。
2. **进入休眠**: 若 2分钟内屏幕未重新点亮，Service 将主动停止网络监听协程 (`stopMonitoring`)，释放 CPU
   锁，允许设备进入深度休眠 (Doze Mode)。
3. **即时唤醒**: 当检测到 **屏幕点亮** 时：
    - 若仍在 2分钟倒计时内，直接取消倒计时，无缝继续。
    - 若已进入休眠状态，立即重启网络监听协程 (`startMonitoring`)，恢复网速更新。

此策略在保证用户体验（点亮屏幕即见网速）的同时，显著降低了待机功耗。
