package vip.mystery0.pixel.meter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import vip.mystery0.pixel.meter.data.source.NetSpeedData
import vip.mystery0.pixel.meter.ui.MainViewModel
import vip.mystery0.pixel.meter.ui.theme.PixelPulseTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PixelPulseTheme {
                HomeScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen() {
        val speed by viewModel.currentSpeed.collectAsState()
        val isServiceRunning by viewModel.isServiceRunning.collectAsState()
        val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsState()
        val isOverlayLocked by viewModel.isOverlayLocked.collectAsState()
        val isLiveUpdateEnabled by viewModel.isLiveUpdateEnabled.collectAsState()
        val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
        val serviceError by viewModel.serviceStartError.collectAsState()

        val context = LocalContext.current

        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SpeedDashboardCard(speed)
                }

                item {
                    Text(
                        stringResource(R.string.title_configuration),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Service Permission Error Card
                if (serviceError != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.error_service_title),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    serviceError?.first ?: stringResource(R.string.error_unknown),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        serviceError?.let { (_, action) ->
                                            val intent = Intent(action)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            when (action) {
                                                Settings.ACTION_APP_NOTIFICATION_SETTINGS -> {
                                                    intent.putExtra(
                                                        Settings.EXTRA_APP_PACKAGE,
                                                        context.packageName
                                                    )
                                                }

                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS -> {
                                                    intent.data =
                                                        "package:${context.packageName}".toUri()
                                                }
                                            }
                                            context.startActivity(intent)
                                            viewModel.clearError()
                                        }
                                    }) {
                                        Text(stringResource(R.string.action_request_fix))
                                    }
                                    Button(
                                        onClick = { viewModel.clearError() },
                                        colors = ButtonDefaults.textButtonColors()
                                    ) {
                                        Text(stringResource(R.string.action_dismiss))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.title_monitor_control),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isServiceRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isServiceRunning) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isServiceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isServiceRunning) stringResource(R.string.status_monitor_running) else stringResource(
                                        R.string.status_monitor_stopped
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isServiceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Start Button
                                Button(
                                    onClick = { viewModel.startService() },
                                    enabled = !isServiceRunning,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.action_start))
                                }
                                // Stop Button
                                Button(
                                    onClick = { viewModel.stopService() },
                                    enabled = isServiceRunning,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.action_stop))
                                }
                            }
                        }
                    }
                }

                item {
                    ConfigRow(
                        title = stringResource(R.string.config_enable_overlay),
                        subtitle = stringResource(R.string.config_enable_overlay_desc),
                        checked = isOverlayEnabled,
                        onCheckedChange = { viewModel.setOverlayEnabled(it) }
                    )
                }

                if (isOverlayEnabled) {
                    item {
                        ConfigRow(
                            title = stringResource(R.string.config_lock_overlay),
                            subtitle = stringResource(R.string.config_lock_overlay_desc),
                            checked = isOverlayLocked,
                            onCheckedChange = { viewModel.setOverlayLocked(it) }
                        )
                    }
                }

                item {
                    ConfigRow(
                        title = stringResource(R.string.config_enable_notification),
                        subtitle = stringResource(R.string.config_enable_notification_desc),
                        checked = isNotificationEnabled,
                        onCheckedChange = { viewModel.setNotificationEnabled(it) }
                    )
                }

                item {
                    ConfigRow(
                        title = stringResource(R.string.config_enable_live_update),
                        subtitle = stringResource(R.string.config_enable_live_update_desc),
                        checked = isLiveUpdateEnabled,
                        onCheckedChange = { viewModel.setLiveUpdateEnabled(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedDashboardCard(speed: NetSpeedData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.label_total_speed),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                formatSpeed(speed.totalSpeed),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.label_download),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "▼ " + formatSpeed(speed.downloadSpeed),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.label_upload),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "▲ " + formatSpeed(speed.uploadSpeed),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun formatSpeed(bytes: Long): String {
    if (bytes < 1024) return "$bytes B/s"
    val kb = bytes / 1024.0
    if (kb < 1000) return "%.0f K/s".format(Locale.US, kb)
    val mb = kb / 1024.0
    if (mb < 1000) return "%.1f M/s".format(Locale.US, mb)
    val gb = mb / 1024.0
    return "%.2f G/s".format(Locale.US, gb)
}
