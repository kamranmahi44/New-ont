package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceEntity
import com.example.ui.components.DeviceTypeIcon
import com.example.ui.components.InspectDeviceDialog
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: MainViewModel
) {
    val devices by viewModel.filteredDevices.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val inspectDevice by viewModel.selectedDeviceForInspect.collectAsState()

    val onlineCount = remember(allDevices) { allDevices.count { it.isOnline && !it.isBlocked } }
    val blockedCount = remember(allDevices) { allDevices.count { it.isBlocked } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("devices_screen")
    ) {
        // Top Search Bar & Header
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connected Devices (${allDevices.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { viewModel.startSubnetScan() },
                        enabled = !isScanning,
                        modifier = Modifier.testTag("devices_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Subnet",
                            tint = if (isScanning) TextMuted else CyberCyanLight
                        )
                    }
                }

                // Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by name, IP, MAC address or vendor...", color = TextMuted) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_device_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Filter Chips
                val chips = listOf(
                    "all" to "All (${allDevices.size})",
                    "online" to "Online ($onlineCount)",
                    "blocked" to "Blocked ($blockedCount)",
                    "laptop" to "Laptops / PCs",
                    "mobile" to "Mobile",
                    "iot" to "Smart Home / IoT",
                    "media" to "Media & TV"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { (filterKey, label) ->
                        val isSelected = selectedFilter == filterKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedFilter.value = filterKey },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyanLight,
                                containerColor = DarkBackground,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyberCyan else DarkSurfaceBorder
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // Devices List
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DevicesOther,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "No matching devices found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Try adjusting your search query or run a subnet scan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices, key = { it.ip }) { device ->
                    DeviceCardItem(
                        device = device,
                        onClick = { viewModel.setInspectDevice(device) },
                        onToggleBlock = { viewModel.toggleBlock(device) },
                        onSetBandwidth = { limit -> viewModel.setBandwidthLimit(device, limit) }
                    )
                }
            }
        }
    }

    // Inspect Device Dialog
    inspectDevice?.let { dev ->
        InspectDeviceDialog(
            device = dev,
            onDismiss = { viewModel.setInspectDevice(null) },
            onToggleBlock = { viewModel.toggleBlock(it) },
            onSetBandwidth = { d, limit -> viewModel.setBandwidthLimit(d, limit) },
            onSetQos = { d, priority -> viewModel.setQosPriority(d, priority) },
            onRename = { d, alias -> viewModel.renameDevice(d, alias) },
            onWakeOnLan = { viewModel.sendWakeOnLan(it) }
        )
    }
}

@Composable
fun DeviceCardItem(
    device: DeviceEntity,
    onClick: () -> Unit,
    onToggleBlock: () -> Unit,
    onSetBandwidth: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("device_card_${device.ip}"),
        color = if (device.isBlocked) RoseBlocked.copy(alpha = 0.08f) else DarkSurface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (device.isBlocked) RoseBlocked.copy(alpha = 0.5f) else DarkSurfaceBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon + Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    DeviceTypeIcon(deviceType = device.deviceType, isBlocked = device.isBlocked)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = device.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (device.qosPriority == "High") {
                                Surface(
                                    color = AmberWarning.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "QOS",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = AmberWarning
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${device.vendor} • ${device.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                StatusPill(isOnline = device.isOnline, isBlocked = device.isBlocked)
            }

            // Specs Row: IP, MAC, Latency
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("IP ADDRESS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                    Text(
                        text = device.ip,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyberCyanLight
                    )
                }

                Column {
                    Text("MAC ADDRESS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                    Text(
                        text = device.mac,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("PING", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                    Text(
                        text = if (device.pingMs > 0) "${device.pingMs} ms" else "N/A",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (device.isOnline) EmeraldOnline else TextMuted
                    )
                }
            }

            // Throughput & Controls Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!device.isBlocked && device.isOnline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Text(
                            text = "%.1f Mb/s".format(device.downloadSpeedMbps),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary
                        )
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(14.dp))
                        Text(
                            text = "%.1f Mb/s".format(device.uploadSpeedMbps),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = if (device.isBlocked) "Device traffic blocked by ACL" else "Device offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (device.isBlocked) RoseBlocked else TextMuted
                    )
                }

                // Block / Allow Switch Button
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.isBlocked) EmeraldOnline else RoseBlocked.copy(alpha = 0.2f),
                        contentColor = if (device.isBlocked) Color.White else RoseBlocked
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = if (!device.isBlocked) androidx.compose.foundation.BorderStroke(1.dp, RoseBlocked.copy(alpha = 0.4f)) else null,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (device.isBlocked) "Restore" else "Block",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
