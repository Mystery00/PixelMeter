package vip.mystery0.pixel.meter.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SliderPreference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TextFieldPreference
import me.zhanghai.compose.preference.TwoTargetPreference
import vip.mystery0.pixel.meter.BuildConfig
import vip.mystery0.pixel.meter.R
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.ui.theme.PixelPulseTheme
import java.util.Locale

class SettingsActivity : ComponentActivity() {
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelPulseTheme {
                SettingsScreen()
            }
        }
    }

    @Composable
    fun SettingsScreen() {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshOverlaySettings()
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_settings)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            ProvidePreferenceLocals {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item { GeneralSection(viewModel) }
                    item { NotificationSection(viewModel) }
                    item { OverlaySection(viewModel) }
                    item { BackgroundSection(viewModel) }
                    item { AboutSection() }
                    item {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val interval by viewModel.samplingInterval.collectAsState(initial = 1500L)
    val isAutoStartEnabled by viewModel.isAutoStartServiceEnabled.collectAsState(initial = false)
    val canEnableAutoStart by viewModel.canEnableAutoStart.collectAsState()
    val hasOverlayPermission by viewModel.canOverlay.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_general)) })

    SliderPreference(
        value = 0F,
        onValueChange = { },
        sliderValue = interval.toFloat(),
        onSliderValueChange = { viewModel.setSamplingInterval(it.toLong()) },
        valueRange = 1000f..3000f,
        valueSteps = 19,
        title = { Text(stringResource(R.string.settings_sampling_interval)) },
        summary = { Text(stringResource(R.string.settings_sampling_interval_desc)) },
        valueText = { Text("${interval}ms") }
    )


    val autoStartSummary = if (canEnableAutoStart) {
        stringResource(R.string.settings_auto_start_service_desc)
    } else {
        stringResource(R.string.settings_auto_start_disabled_reason)
    }

    SwitchPreference(
        value = isAutoStartEnabled,
        onValueChange = { viewModel.setAutoStartServiceEnabled(it) },
        enabled = canEnableAutoStart,
        title = { Text(stringResource(R.string.settings_auto_start_service_title)) },
        summary = { Text(autoStartSummary) }
    )

    // Permission Indicators
    val overlayPermissionSummary = if (hasOverlayPermission) {
        stringResource(R.string.settings_permission_granted)
    } else {
        stringResource(R.string.settings_permission_denied)
    }
    Preference(
        title = { Text(stringResource(R.string.settings_permission_overlay)) },
        summary = { Text(overlayPermissionSummary) },
        onClick = {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }
    )

    val notificationPermissionSummary = if (hasNotificationPermission) {
        stringResource(R.string.settings_permission_granted)
    } else {
        stringResource(R.string.settings_permission_denied)
    }
    Preference(
        title = { Text(stringResource(R.string.settings_permission_notification)) },
        summary = { Text(notificationPermissionSummary) },
        onClick = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
        }
    )
}

@Composable
fun BackgroundSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val isIgnoringBatteryOptimizations by viewModel.isIgnoringBatteryOptimizations.collectAsState()
    val isHideFromRecents by viewModel.isHideFromRecents.collectAsState(initial = false)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_background)) })

    Preference(
        title = { Text(stringResource(R.string.settings_ignore_battery_optimizations_title)) },
        summary = {
            Text(
                if (isIgnoringBatteryOptimizations) stringResource(R.string.settings_ignore_battery_optimizations_on)
                else stringResource(R.string.settings_ignore_battery_optimizations_off)
            )
        },
        onClick = {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }
    )

    SwitchPreference(
        value = isHideFromRecents,
        onValueChange = { viewModel.setHideFromRecents(it) },
        title = { Text(stringResource(R.string.settings_hide_from_recents_title)) },
        summary = { Text(stringResource(R.string.settings_hide_from_recents_desc)) }
    )

    Preference(
        title = { Text(stringResource(R.string.settings_dont_kill_my_app_title)) },
        summary = { Text(stringResource(R.string.settings_dont_kill_my_app_desc)) },
        onClick = {
            val url = "https://dontkillmyapp.com/"
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, url.toUri())
        }
    )
}

@Composable
fun OverlaySection(viewModel: SettingsViewModel) {
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val canOverlay by viewModel.canOverlay.collectAsState()

    val isEnabled by viewModel.isOverlayEnabled.collectAsState(initial = false)
    val isLocked by viewModel.isOverlayLocked.collectAsState(initial = false)
    val isOverlayUseDefaultColors by viewModel.isOverlayUseDefaultColors.collectAsState(initial = false)
    val bgColor by viewModel.overlayBgColor.collectAsState(initial = 0)
    val textColor by viewModel.overlayTextColor.collectAsState(initial = 0)
    val cornerRadius by viewModel.overlayCornerRadius.collectAsState(initial = 8)
    val textSize by viewModel.overlayTextSize.collectAsState(initial = 10f)
    val textUp by viewModel.overlayTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.overlayTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.overlayOrderUpFirst.collectAsState(initial = true)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_overlay)) })
    val isSwitchEnabled = !isServiceRunning || canOverlay
    val summaryText = if (isSwitchEnabled) {
        stringResource(R.string.config_enable_overlay_desc)
    } else {
        stringResource(R.string.config_overlay_disabled_reason)
    }
    SwitchPreference(
        value = isEnabled,
        onValueChange = { viewModel.setOverlayEnabled(it) },
        enabled = isSwitchEnabled,
        title = { Text(stringResource(R.string.config_enable_overlay)) },
        summary = { Text(summaryText) }
    )

    if (isEnabled) {
        SwitchPreference(
            value = isLocked,
            onValueChange = { viewModel.setOverlayLocked(it) },
            title = { Text(stringResource(R.string.settings_lock_overlay)) },
            summary = { Text(stringResource(R.string.config_lock_overlay_desc)) }
        )
        SwitchPreference(
            value = isOverlayUseDefaultColors,
            onValueChange = { viewModel.setOverlayUseDefaultColors(it) },
            title = { Text(stringResource(R.string.settings_overlay_use_default_colors)) },
            summary = { Text(stringResource(R.string.settings_overlay_use_default_colors_desc)) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_bg_color),
            color = Color(bgColor),
            enabled = !isOverlayUseDefaultColors,
            onColorSelected = { viewModel.setOverlayBgColor(it.toArgb()) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_text_color),
            color = Color(textColor),
            enabled = !isOverlayUseDefaultColors,
            onColorSelected = { viewModel.setOverlayTextColor(it.toArgb()) }
        )
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = cornerRadius.toFloat(),
            onSliderValueChange = { viewModel.setOverlayCornerRadius(it.toInt()) },
            valueRange = 0f..32f,
            valueSteps = 32,
            title = { Text(stringResource(R.string.settings_overlay_corner_radius)) },
            valueText = { Text("${cornerRadius}dp") }
        )
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = textSize,
            onSliderValueChange = { viewModel.setOverlayTextSize(it) },
            valueRange = 8f..24f,
            title = { Text(stringResource(R.string.settings_overlay_text_size)) },
            valueText = { Text("${"%.1f".format(Locale.getDefault(), textSize)}sp") }
        )
        TextFieldPreference(
            value = textUp,
            onValueChange = { viewModel.setOverlayTextUp(it) },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_up)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_up_desc, textUp)) },
        )
        TextFieldPreference(
            value = textDown,
            onValueChange = { viewModel.setOverlayTextDown(it) },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_down)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_down_desc, textDown)) },
        )
        SwitchPreference(
            value = upFirst,
            onValueChange = { viewModel.setOverlayOrderUpFirst(it) },
            title = { Text(stringResource(R.string.settings_show_up_first)) },
            summary = { Text(stringResource(R.string.settings_show_up_first_desc)) }
        )
    }
}

@Composable
fun NotificationSection(viewModel: SettingsViewModel) {
    val isEnabled by viewModel.isNotificationEnabled.collectAsState(initial = true)
    val isLiveUpdateEnabled by viewModel.isLiveUpdateEnabled.collectAsState(initial = false)
    val textUp by viewModel.notificationTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.notificationTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.notificationOrderUpFirst.collectAsState(initial = true)
    val displayMode by viewModel.notificationDisplayMode.collectAsState(initial = 0)
    val textSize by viewModel.notificationTextSize.collectAsState(initial = 0.65f)
    val unitSize by viewModel.notificationUnitSize.collectAsState(initial = 0.35f)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_notification)) })
    SwitchPreference(
        value = isEnabled,
        onValueChange = { viewModel.setNotificationEnabled(it) },
        title = { Text(stringResource(R.string.config_enable_notification)) },
        summary = { Text(stringResource(R.string.config_enable_notification_desc)) }
    )

    if (isEnabled) {
        SwitchPreference(
            value = isLiveUpdateEnabled,
            onValueChange = { viewModel.setLiveUpdateEnabled(it) },
            title = { Text(stringResource(R.string.config_enable_live_update)) },
            summary = { Text(stringResource(R.string.config_enable_live_update_desc)) }
        )
        TextFieldPreference(
            value = textUp,
            onValueChange = { viewModel.setNotificationTextUp(it) },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_up)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_up_desc, textUp)) },
        )
        TextFieldPreference(
            value = textDown,
            onValueChange = { viewModel.setNotificationTextDown(it) },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_down)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_down_desc, textDown)) },
        )
        SwitchPreference(
            value = upFirst,
            onValueChange = { viewModel.setNotificationOrderUpFirst(it) },
            title = { Text(stringResource(R.string.settings_show_up_first)) },
            summary = { Text(stringResource(R.string.settings_show_up_first_desc)) }
        )

        val labelTotal = stringResource(R.string.settings_display_mode_total)
        val labelUpload = stringResource(R.string.settings_display_mode_upload)
        val labelDownload = stringResource(R.string.settings_display_mode_download)

        val displayModeLabel = when (displayMode) {
            1 -> labelUpload
            2 -> labelDownload
            else -> labelTotal
        }
        ListPreference(
            value = displayModeLabel,
            onValueChange = {
                val mode = when (it) {
                    labelUpload -> 1
                    labelDownload -> 2
                    else -> 0
                }
                viewModel.setNotificationDisplayMode(mode)
            },
            title = { Text(stringResource(R.string.settings_notification_display_mode)) },
            values = listOf(
                labelTotal,
                labelUpload,
                labelDownload
            ),
            summary = { Text(displayModeLabel) }
        )

        SliderPreference(
            enabled = !isLiveUpdateEnabled,
            value = 0F,
            onValueChange = { },
            sliderValue = textSize,
            onSliderValueChange = { viewModel.setNotificationTextSize(it) },
            valueRange = 0.1f..1.0f,
            title = { Text(stringResource(R.string.settings_notification_text_size)) },
            valueText = { Text("%.2f".format(textSize)) }
        )

        SliderPreference(
            enabled = !isLiveUpdateEnabled,
            value = 0F,
            onValueChange = { },
            sliderValue = unitSize,
            onSliderValueChange = { viewModel.setNotificationUnitSize(it) },
            valueRange = 0.1f..1.0f,
            title = { Text(stringResource(R.string.settings_notification_unit_size)) },
            valueText = { Text("%.2f".format(unitSize)) }
        )

        // Threshold Settings
        val threshold by viewModel.notificationThreshold.collectAsState(initial = 0L)
        val lowTrafficMode by viewModel.notificationLowTrafficMode.collectAsState(initial = 0)

        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = threshold.toFloat() / 1024,
            onSliderValueChange = { viewModel.setNotificationThreshold((it * 1024).toLong()) },
            valueRange = 0f..1024f, // 0KB to 1024KB (1MB)
            valueSteps = 20, // 50KB steps roughly
            title = { Text(stringResource(R.string.settings_notification_threshold)) },
            summary = {
                if (threshold == 0L) {
                    Text(stringResource(R.string.settings_notification_threshold_disabled))
                } else {
                    val thresholdStr =
                        NetworkRepository.formatSpeedLine(
                            threshold
                        )
                    Text(
                        stringResource(
                            R.string.settings_notification_threshold_desc,
                            thresholdStr
                        )
                    )
                }
            },
            valueText = {
                Text(
                    NetworkRepository.formatSpeedLine(
                        threshold
                    )
                )
            }
        )

        TextFieldPreference(
            value = (threshold / 1024).toString(),
            onValueChange = {
                val kb = it.toLongOrNull()
                if (kb != null && kb >= 0) {
                    viewModel.setNotificationThreshold(kb * 1024)
                }
            },
            title = { Text(stringResource(R.string.settings_notification_threshold_input_title)) },
            summary = { Text(stringResource(R.string.settings_notification_threshold_input_summary)) },
            textToValue = { it },
        )

        val labelStatic = stringResource(R.string.settings_notification_low_traffic_mode_static)
        val labelDynamic = stringResource(R.string.settings_notification_low_traffic_mode_dynamic)
        val lowTrafficModeLabel = when (lowTrafficMode) {
            1 -> labelDynamic
            else -> labelStatic
        }

        ListPreference(
            value = lowTrafficModeLabel,
            onValueChange = {
                val mode = when (it) {
                    labelDynamic -> 1
                    else -> 0
                }
                viewModel.setNotificationLowTrafficMode(mode)
            },
            title = { Text(stringResource(R.string.settings_notification_low_traffic_mode)) },
            values = listOf(
                labelStatic,
                labelDynamic
            ),
            summary = {
                Column {
                    Text(lowTrafficModeLabel)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_notification_low_traffic_mode_explanation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        // Notification Color Settings
        val useCustomColor by viewModel.notificationUseCustomColor.collectAsState(initial = false)
        val notificationColor by viewModel.notificationColor.collectAsState(initial = 0)

        SwitchPreference(
            value = useCustomColor,
            onValueChange = { viewModel.setNotificationUseCustomColor(it) },
            title = { Text(stringResource(R.string.settings_notification_use_custom_color_title)) },
            summary = { Text(stringResource(R.string.settings_notification_use_custom_color_desc)) }
        )

        ColorPreference(
            title = stringResource(R.string.settings_notification_color_title),
            color = Color(notificationColor),
            enabled = useCustomColor,
            onColorSelected = { viewModel.setNotificationColor(it.toArgb()) }
        )
    }
}

@Composable
fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_about)) })
    Preference(
        title = { Text(stringResource(R.string.settings_app_version)) },
        summary = { Text(BuildConfig.VERSION_NAME) }
    )
    Preference(
        title = { Text(stringResource(R.string.settings_github)) },
        summary = { Text("https://github.com/Mystery00/PixelMeter") },
        onClick = { uriHandler.openUri("https://github.com/Mystery00/PixelMeter") }
    )
}

@Composable
fun ColorPreference(
    title: String,
    color: Color,
    enabled: Boolean = true,
    onColorSelected: (Color) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val theme = LocalPreferenceTheme.current

    TwoTargetPreference(
        title = { Text(title) },
        enabled = enabled,
        secondTarget = {
            Box(
                modifier = Modifier
                    .padding(horizontal = theme.horizontalSpacing)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        },
        onClick = { if (enabled) showDialog = true }
    )
    if (showDialog) {
        val controller = rememberColorPickerController()
        var selectedColor by remember { mutableStateOf(color) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.color_picker_title)) },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        controller = controller,
                        initialColor = color,
                        onColorChanged = { envelope ->
                            selectedColor = envelope.color
                        }
                    )
                    AlphaSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        controller = controller,
                    )
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        controller = controller,
                    )
                    AlphaTile(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(MaterialTheme.shapes.medium),
                        controller = controller
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onColorSelected(selectedColor)
                    showDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}