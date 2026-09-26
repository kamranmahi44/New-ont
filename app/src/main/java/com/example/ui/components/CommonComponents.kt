package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceEntity
import com.example.ui.theme.*

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeviceTypeIcon(deviceType: String, isBlocked: Boolean, modifier: Modifier = Modifier) {
    val (icon, tint, bg) = when {
        isBlocked -> Triple(Icons.Default.Block, RoseBlocked, RoseBlocked.copy(alpha = 0.15f))
        deviceType == "router" -> Triple(Icons.Default.Router, CyberCyanLight, CyberCyan.copy(alpha = 0.15f))
        deviceType == "laptop" -> Triple(Icons.Default.Laptop, IndigoLight, IndigoAccent.copy(alpha = 0.15f))
        deviceType == "mobile" -> Triple(Icons.Default.Smartphone, EmeraldOnline, EmeraldOnline.copy(alpha = 0.15f))
        deviceType == "media" -> Triple(Icons.Default.Tv, Color(0xFFC084FC), Color(0xFF9333EA).copy(alpha = 0.15f))
        deviceType == "gaming" -> Triple(Icons.Default.SportsEsports, Color(0xFFE879F9), Color(0xFFC026D3).copy(alpha = 0.15f))
        deviceType == "iot" -> Triple(Icons.Default.Sensors, AmberWarning, AmberWarning.copy(alpha = 0.15f))
        else -> Triple(Icons.Default.Devices, TextSecondary, DarkSurfaceBorder)
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = deviceType,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun StatusPill(isOnline: Boolean, isBlocked: Boolean, modifier: Modifier = Modifier) {
    val (text, color, bgColor) = when {
        isBlocked -> Triple("BLOCKED", RoseBlocked, RoseBlocked.copy(alpha = 0.15f))
        isOnline -> Triple("ACTIVE", EmeraldOnline, EmeraldOnline.copy(alpha = 0.15f))
        else -> Triple("OFFLINE", TextMuted, DarkSurfaceElevated)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectDeviceDialog(
    device: DeviceEntity,
    onDismiss: () -> Unit,
    onToggleBlock: (DeviceEntity) -> Unit,
    onSetBandwidth: (DeviceEntity, Int) -> Unit,
    onSetQos: (DeviceEntity, String) -> Unit,
    onRename: (DeviceEntity, String) -> Unit,
    onWakeOnLan: (DeviceEntity) -> Unit
) {
    var editAlias by remember { mutableStateOf(device.alias) }
    var isEditingAlias by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeviceTypeIcon(deviceType = device.deviceType, isBlocked = device.isBlocked)
                Column {
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${device.vendor} • ${device.deviceType.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // IP & MAC Box
                Surface(
                    color = DarkBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IP Address:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(device.ip, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = CyberCyanLight)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MAC Address:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(device.mac, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = TextSecondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ping Latency:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("${device.pingMs} ms", style = MaterialTheme.typography.bodySmall, color = EmeraldOnline)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Open Ports:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                if (device.openPortsString.isNotBlank()) device.openPortsString else "None detected",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = CyberCyanLight
                            )
                        }
                    }
                }

                // Edit Alias
                if (isEditingAlias) {
                    OutlinedTextField(
                        value = editAlias,
                        onValueChange = { editAlias = it },
                        label = { Text("Device Custom Alias") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                onRename(device, editAlias)
                                isEditingAlias = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = CyberCyan)
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custom Name:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        TextButton(onClick = { isEditingAlias = true }) {
                            Text(if (device.alias.isNotBlank()) device.alias else "Set Alias", color = CyberCyanLight)
                        }
                    }
                }

                // QoS Selection
                Text("QoS Traffic Priority:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Normal", "High").forEach { priority ->
                        val selected = device.qosPriority == priority
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetQos(device, priority) },
                            color = if (selected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) CyberCyan else DarkSurfaceBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = priority,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) CyberCyanLight else TextSecondary
                            )
                        }
                    }
                }

                // Bandwidth Cap Selector
                Text("Bandwidth Limit:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Unlimited", 50 to "50M", 10 to "10M", 2 to "2M").forEach { (limit, label) ->
                        val selected = device.bandwidthLimitMbps == limit
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetBandwidth(device, limit) },
                            color = if (selected) IndigoAccent.copy(alpha = 0.2f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) IndigoLight else DarkSurfaceBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) IndigoLight else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onToggleBlock(device)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (device.isBlocked) EmeraldOnline else RoseBlocked
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (device.isBlocked) "Restore Access" else "Block from Network")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onWakeOnLan(device)
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Text("Wake-on-LAN", color = TextSecondary)
            }
        }
    )
}
