package vip.mystery0.pixelpulse.ui.home

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.androidx.compose.koinViewModel
import rikka.shizuku.Shizuku
import vip.mystery0.pixelpulse.data.source.NetSpeedData
import vip.mystery0.pixelpulse.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: MainViewModel = koinViewModel()

    val speed by viewModel.currentSpeed.collectAsState()
    val isShizuku by viewModel.isShizukuMode.collectAsState()
    val isGranted by viewModel.shizukuPermissionGranted.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val blacklist by viewModel.blacklistedInterfaces.collectAsState()
    val serviceError by viewModel.serviceStartError.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.checkUsagePermission()
    }

    // Resume check
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkUsagePermission()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pixel Pulse") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isShizuku && !hasUsagePermission) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Usage Access Required",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Standard mode needs 'Usage Access' permission to read network stats.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Button(onClick = {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            }) {
                                Text("Open Settings")
                            }
                        }
                    }
                }
            }

            item {
                SpeedDashboardCard(speed)
            }

            item {
                Text(
                    "Configuration",
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
                                "Service Error",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                serviceError?.first ?: "Unknown error",
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
                                    Text("Request / Fix")
                                }
                                Button(
                                    onClick = { viewModel.clearError() },
                                    colors = ButtonDefaults.textButtonColors()
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Monitor Control",
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
                                text = if (isServiceRunning) "Monitor is Running" else "Monitor is Stopped",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isServiceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Start Button
                            androidx.compose.material3.Button(
                                onClick = { viewModel.startService() },
                                enabled = !isServiceRunning,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start")
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
                                Text("Stop")
                            }
                        }
                    }
                }
            }

            item {
                ConfigRow(
                    title = "Enable Overlay",
                    subtitle = "Show floating window",
                    checked = isOverlayEnabled,
                    onCheckedChange = { viewModel.setOverlayEnabled(it) }
                )
            }

            item {
                ConfigRow(
                    title = "Shizuku Mode",
                    subtitle = "Use Binder IPC for precision",
                    checked = isShizuku,
                    onCheckedChange = { viewModel.setShizukuMode(it) }
                )
            }

            if (isShizuku && !isGranted) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Shizuku permission required",
                                style = MaterialTheme.typography.labelLarge
                            )
                            if (!safeShizukuPing()) {
                                Text(
                                    "Shizuku is not running or not installed.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Button(onClick = { viewModel.requestShizukuPermission() }) {
                                    Text("Request Permission")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Interface Blacklist",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                BlacklistEditor(
                    items = blacklist,
                    onAdd = { viewModel.addToBlacklist(it) },
                    onRemove = { viewModel.removeFromBlacklist(it) }
                )
            }
        }
    }
}

fun safeShizukuPing(): Boolean {
    return try {
        Shizuku.pingBinder()
    } catch (e: Throwable) {
        false
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
            Text("Total Speed", style = MaterialTheme.typography.labelMedium)
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
                    Text("Download", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "▼ " + formatSpeed(speed.downloadSpeed),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Upload", style = MaterialTheme.typography.bodySmall)
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

@Composable
fun BlacklistEditor(items: Set<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Interface Name (e.g. tun0)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        // FlowRow is experimental, using simple Column of Rows or just manual wrapping logic?
        // Or simply `ContextualFlowRow` (new) or just normal Layout.
        // I'll just use a Column of chips if few, but better to use keys.
        // I'll assume only a few items.

        Spacer(modifier = Modifier.height(8.dp))

        items.forEach { iface ->
            InputChip(
                selected = true,
                onClick = { },
                label = { Text(iface) },
                trailingIcon = {
                    IconButton(onClick = { onRemove(iface) }, modifier = Modifier.height(18.dp)) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        }
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
