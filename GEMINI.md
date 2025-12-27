## 代码质量与开发原则

### 质量要求

- **语言规范**: 严格遵循 Kotlin 官方编码规范。
- **Android 规范**:
    - 遵循 Modern Android Development (MAD) 指南。
  - **UI 框架**: 全面使用 Jetpack Compose (+ Material3/Material You/Material Express)，支持 Pixel
    动态取色。
    - **架构**: MVVM (ViewModel + StateFlow + Repository)。
    - **依赖注入**: 使用 **Koin** (Koin-Android, Koin-Compose)。
    - **兼容性**: MinSDK 为 36 (Android 16)，TargetSDK 为 Latest (36)。
- **功能特性规范**:
  - **单一数据源**:
    - 使用 `TrafficStats` 配合 `ConnectivityManager`。
    - **核心逻辑**: 通过 `ConnectivityManager` 遍历物理网络接口 (Wi-Fi, Cellular, Ethernet)，排除 VPN
      虚拟接口 (避免流量双重统计)，直接读取 `TrafficStats` 数据。
    - **严格禁止**: 使用 Root 或 Shizuku 权限进行非必要的底层操作。
    - **UI 呈现**:
      - 首次启动不开启显示，由用户选择。
      - 通知栏动态图标 (Notification Icon): 实时绘制 Bitmap。双向模式下合并展示总量。
        - 悬浮窗 (Floating Window): 使用 Compose 挂载到 WindowManager，支持独立开关。
- **代码结构**:
    - 单个文件原则上不超过 1000 行。
    - **Service 分离**: 网速监听核心逻辑需运行在前台服务 (Foreground Service, type=`dataSync`) 中。
    - 避免在 Activity 中编写业务逻辑。
- **注释**:
    - 使用中文编写清晰的 KDoc 与行内注释。
  - 核心算法逻辑（如网络接口过滤）必须添加详细注释说明。

### 测试与验证

- **编译检查**:
    - 变更代码后，必须执行 `./gradlew :app:assembleDebug` 确保编译通过。
- **Lint 检查**:
    - 运行 `./gradlew lint` 检查潜在的代码质量问题。
- **功能验证**:
  - 重点测试开启 VPN 场景下的网速统计是否准确（不应包含 VPN 虚拟网卡流量）。
  - 必须在真机（Pixel 设备优先）上测试。

#### 项目本地校验流程

1.  命令行执行 `./gradlew :app:assembleDebug`。
2.  确认无编译错误，且 `libs.versions.toml` 中无过时警告。

## 文档与记忆

文档与记忆采用 Markdown 格式，存放于 `.agentdocs/` 及其子目录下。
索引文档：`.agentdocs/index.md`

### 文档分类

- `prd/` - 产品与需求
  - `prd/requirements.md` - 核心功能需求 (PixelMeter 功能清单)
- `architecture/` - 架构与技术细节
    - `architecture/data-source-strategy.md` - 混合数据源策略 (Shizuku vs Standard API)
    - `architecture/service-lifecycle.md` - 前台服务保活与 Android 14+ 适配
- `ui/` - 界面规范
    - `ui/design-system.md` - Material3 主题与悬浮窗设计规范
- `workflow/` - 任务流文档 (按标准格式命名)

### 全局重要记忆

- **项目名称**: PixelMeter
- **设备支持**:
    - 核心目标: Google Pixel 系列 (Android 10+)。
    - 兼容目标: 运行原生/类原生 Android (AOSP) 的设备。
- **SDK 版本**:
  - **MinSDK**: 36 (Android 16)。
  - **CompileSDK/TargetSDK**: 36 (Android 16)。
- **关键技术决策**:
    - **DI**: Koin (轻量级，适合本工具)。
    - **Browser**: Chrome Custom Tabs (CCT) 用于集成 Cloudflare 测速。


## 任务处理指南

- **需求澄清**: 当需求不明确时（例如具体的过滤接口名称、悬浮窗的吸附逻辑），先提问澄清。
- **方案分析**: 对于用户提出的设想，需结合 Android 系统限制（特别是后台限制）进行分析。
- **分阶段实施**:
    1.  基础架构搭建 (Koin, Compose, Navigation)。
  2. 网速监听实现 (TrafficStats + ConnectivityManager)。
  3. UI 层实现 (通知栏绘图、悬浮窗)。
  4. CCT 集成与设置页完善。
- **风险记录**: 涉及系统底层 (如读取 `/proc`) 或保活策略修改时，需在文档中记录潜在的兼容性风险。

### 任务回顾

- 在任务完成呈现最终消息前，必须进行以下任务回顾：
    - 检查是否产生新的可复用组件，并更新架构文档。
    - 检查 `.agentdocs/` 下的文档是否需要更新。
    - 确认 `libs.versions.toml` 中的依赖版本是否为最新稳定版。

## 沟通原则

- 与用户的所有回复与沟通，文档与代码注释均使用中文。
- 涉及专业术语 (如 "Interface", "Tun", "Binder", "Overlay") 时保留英文，必要时附带中文解释。