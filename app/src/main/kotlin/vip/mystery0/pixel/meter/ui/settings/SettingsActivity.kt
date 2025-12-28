package vip.mystery0.pixel.meter.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                    item { OverlaySection(viewModel) }
                    item { NotificationSection(viewModel) }
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
    val interval by viewModel.samplingInterval.collectAsState(initial = 1500L)

    var intervalSlider by remember { mutableFloatStateOf(interval.toFloat()) }

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_general)) })

    SliderPreference(
        value = interval.toFloat(),
        onValueChange = { viewModel.setSamplingInterval(it.toLong()) },
        sliderValue = intervalSlider,
        onSliderValueChange = { intervalSlider = it },
        valueRange = 1000f..5000f,
        valueSteps = 39,
        title = { Text(stringResource(R.string.settings_sampling_interval)) },
        summary = { Text(stringResource(R.string.settings_sampling_interval_desc)) },
        valueText = { Text("${intervalSlider.toLong()}ms") }
    )
}

@Composable
fun OverlaySection(viewModel: SettingsViewModel) {
    val isEnabled by viewModel.isOverlayEnabled.collectAsState(initial = false)
    val isLocked by viewModel.isOverlayLocked.collectAsState(initial = false)
    val bgColor by viewModel.overlayBgColor.collectAsState(initial = 0)
    val textColor by viewModel.overlayTextColor.collectAsState(initial = 0)
    val cornerRadius by viewModel.overlayCornerRadius.collectAsState(initial = 8)
    val textSize by viewModel.overlayTextSize.collectAsState(initial = 10f)
    val textUp by viewModel.overlayTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.overlayTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.overlayOrderUpFirst.collectAsState(initial = true)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_overlay)) })
    SwitchPreference(
        value = isEnabled,
        onValueChange = { viewModel.setOverlayEnabled(it) },
        title = { Text(stringResource(R.string.config_enable_overlay)) },
        summary = { Text(stringResource(R.string.config_enable_overlay_desc)) }
    )

    if (isEnabled) {
        SwitchPreference(
            value = isLocked,
            onValueChange = { viewModel.setOverlayLocked(it) },
            title = { Text(stringResource(R.string.settings_lock_overlay)) },
            summary = { Text(stringResource(R.string.config_lock_overlay_desc)) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_bg_color),
            color = Color(bgColor),
            onColorSelected = { viewModel.setOverlayBgColor(it.toArgb()) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_text_color),
            color = Color(textColor),
            onColorSelected = { viewModel.setOverlayTextColor(it.toArgb()) }
        )
        SliderPreference(
            value = cornerRadius.toFloat(),
            onValueChange = { viewModel.setOverlayCornerRadius(it.toInt()) },
            sliderValue = cornerRadius.toFloat(),
            onSliderValueChange = { viewModel.setOverlayCornerRadius(it.toInt()) },
            valueRange = 0f..32f,
            valueSteps = 32,
            title = { Text(stringResource(R.string.settings_overlay_corner_radius)) },
            valueText = { Text("${cornerRadius.toInt()}dp") }
        )
        SliderPreference(
            value = textSize,
            onValueChange = { viewModel.setOverlayTextSize(it) },
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
    }
}

// Helper to get string inside Composable context where possible, or use Context
@Composable
fun getString(id: Int): String {
    return stringResource(id)
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
    onColorSelected: (Color) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val theme = LocalPreferenceTheme.current

    TwoTargetPreference(
        title = { Text(title) },
        secondTarget = {
            Box(
                modifier = Modifier
                    .padding(horizontal = theme.horizontalSpacing)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        },
        onClick = { showDialog = true }
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