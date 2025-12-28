package vip.mystery0.pixel.meter.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import vip.mystery0.pixel.meter.BuildConfig
import vip.mystery0.pixel.meter.ui.theme.PixelPulseTheme

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
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                GeneralSection(viewModel)
                HorizontalDivider()
                OverlaySection(viewModel)
                HorizontalDivider()
                NotificationSection(viewModel)
                HorizontalDivider()
                AboutSection()
            }
        }
    }
}

@Composable
fun GeneralSection(viewModel: SettingsViewModel) {
    val interval by viewModel.samplingInterval.collectAsState(initial = 1500L)

    SectionHeader(title = "General")

    ListItem(
        headlineContent = { Text("Sampling Interval") },
        supportingContent = {
            Text("${interval}ms\nLower values update faster but use more battery. Higher values save battery but update slower.")
        },
        trailingContent = {
            // Simple slider dialog or similar could go here, for now just a basic implementation inline or separate
        }
    )

    // Slider for Sampling Interval
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Slider(
            value = interval.toFloat(),
            onValueChange = { viewModel.setSamplingInterval(it.toLong()) },
            valueRange = 1000f..5000f,
            steps = 39 // (5000-1000)/100 = 40 steps -> 40 intervals -> 39 steps param
        )
    }
}

@Composable
fun OverlaySection(viewModel: SettingsViewModel) {
    val isEnabled by viewModel.isOverlayEnabled.collectAsState(initial = false)
    val isLocked by viewModel.isOverlayLocked.collectAsState(initial = false)
    val bgColor by viewModel.overlayBgColor.collectAsState(initial = 0)
    val cornerRadius by viewModel.overlayCornerRadius.collectAsState(initial = 8)
    val textSize by viewModel.overlayTextSize.collectAsState(initial = 10f)
    val textUp by viewModel.overlayTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.overlayTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.overlayOrderUpFirst.collectAsState(initial = true)

    SectionHeader(title = "Overlay")

    ListItem(
        headlineContent = { Text("Enable Overlay") },
        trailingContent = {
            Switch(checked = isEnabled, onCheckedChange = { viewModel.setOverlayEnabled(it) })
        }
    )

    if (isEnabled) {
        ListItem(
            headlineContent = { Text("Lock Position") },
            trailingContent = {
                Switch(checked = isLocked, onCheckedChange = { viewModel.setOverlayLocked(it) })
            }
        )

        // Background Color Picker (Simplified)
        ColorPickerItem(
            title = "Background Color",
            currentColor = Color(bgColor),
            onColorSelected = { viewModel.setOverlayBgColor(it.toArgb()) }
        )

        // Corner Radius
        ListItem(
            headlineContent = { Text("Corner Radius: $cornerRadius dp") }
        )
        Slider(
            value = cornerRadius.toFloat(),
            onValueChange = { viewModel.setOverlayCornerRadius(it.toInt()) },
            valueRange = 0f..32f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Text Size
        ListItem(
            headlineContent = { Text("Text Size: ${String.format("%.1f", textSize)} sp") }
        )
        Slider(
            value = textSize,
            onValueChange = { viewModel.setOverlayTextSize(it) },
            valueRange = 8f..24f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Text Customization
        TextInputItem(
            title = "Up Prefix",
            value = textUp,
            onValueChange = { viewModel.setOverlayTextUp(it) })
        TextInputItem(
            title = "Down Prefix",
            value = textDown,
            onValueChange = { viewModel.setOverlayTextDown(it) })

        ListItem(
            headlineContent = { Text("Show Up First") },
            supportingContent = { Text("Toggle order of up/down speed") },
            trailingContent = {
                Switch(
                    checked = upFirst,
                    onCheckedChange = { viewModel.setOverlayOrderUpFirst(it) })
            }
        )
    }
}

@Composable
fun NotificationSection(viewModel: SettingsViewModel) {
    val isEnabled by viewModel.isNotificationEnabled.collectAsState(initial = true)
    val textUp by viewModel.notificationTextUp.collectAsState(initial = "TX ")
    val textDown by viewModel.notificationTextDown.collectAsState(initial = "RX ")
    val upFirst by viewModel.notificationOrderUpFirst.collectAsState(initial = true)
    val displayMode by viewModel.notificationDisplayMode.collectAsState(initial = 0)

    SectionHeader(title = "Notification")

    ListItem(
        headlineContent = { Text("Enable Notification") },
        trailingContent = {
            Switch(checked = isEnabled, onCheckedChange = { viewModel.setNotificationEnabled(it) })
        }
    )

    if (isEnabled) {
        TextInputItem(
            title = "Up Prefix",
            value = textUp,
            onValueChange = { viewModel.setNotificationTextUp(it) })
        TextInputItem(
            title = "Down Prefix",
            value = textDown,
            onValueChange = { viewModel.setNotificationTextDown(it) })

        ListItem(
            headlineContent = { Text("Show Up First") },
            trailingContent = {
                Switch(
                    checked = upFirst,
                    onCheckedChange = { viewModel.setNotificationOrderUpFirst(it) })
            }
        )

        // Display Mode
        // 0: Total, 1: Up Only, 2: Down Only
        ListItem(
            headlineContent = { Text("Display Content") },
            supportingContent = {
                val modeText = when (displayMode) {
                    0 -> "Total"
                    1 -> "Upload Only"
                    2 -> "Download Only"
                    else -> "Total"
                }
                Text(modeText)
            },
            modifier = Modifier.clickable {
                viewModel.setNotificationDisplayMode((displayMode + 1) % 3)
            }
        )
    }
}

@Composable
fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    SectionHeader(title = "About")

    ListItem(
        headlineContent = { Text("Version") },
        supportingContent = { Text(BuildConfig.VERSION_NAME) }
    )

    ListItem(
        headlineContent = { Text("GitHub") },
        supportingContent = { Text("https://github.com/Mystery00/PixelMeter") },
        modifier = Modifier.clickable {
            uriHandler.openUri("https://github.com/Mystery00/PixelMeter")
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun TextInputItem(title: String, value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value.ifEmpty { "(Empty)" }) },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        var text by remember { mutableStateOf(value) }
        BasicAlertDialog(
            onDismissRequest = { showDialog = false },
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Edit $title", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.align(Alignment.End)) {
                        Button(onClick = { showDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = {
                            onValueChange(text)
                            showDialog = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerItem(title: String, currentColor: Color, onColorSelected: (Color) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .clickable { showDialog = true }
            )
        }
    )

    if (showDialog) {
        // Very basic color picker dialog (Predefined colors + Alpha)
        val colors = listOf(
            Color.Black, Color.DarkGray, Color.Gray,
            Color.Red, Color.Blue, Color.Green,
            Color.Transparent
        )

        BasicAlertDialog(onDismissRequest = { showDialog = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Color")
                    Spacer(modifier = Modifier.height(8.dp))
                    // Alpha Slider
                    var alpha by remember { mutableFloatStateOf(currentColor.alpha) }
                    Text("Alpha: ${(alpha * 100).toInt()}%")
                    Slider(value = alpha, onValueChange = { alpha = it })

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple grid for example
                    Row {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        onColorSelected(color.copy(alpha = alpha))
                                        showDialog = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}