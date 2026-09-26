package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.DeviceEntity
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToDevices: () -> Unit,
    onNavigateToWebDashboard: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val wifiInfo by viewModel.wifiInfo.collectAsState()
    val adminProfile by viewModel.adminProfile.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val context = LocalContext.current
    var showRebootDialog by remember { mutableStateOf(false) }
    var guestWifiEnabled by remember { mutableStateOf(false) }

    val activeCount = allDevices.count { it.isOnline && !it.isBlocked }
    val blockedCount = allDevices.count { it.isBlocked }
    val totalDownload = allDevices.filter { !it.isBlocked }.sumOf { it.downloadSpeedMbps.toDouble() }
    val totalUpload = allDevices.filter { !it.isBlocked }.sumOf { it.uploadSpeedMbps.toDouble() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: NetAdmin Branding & Admin Profile Chip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NetAdmin",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = TextPrimary
                        )
                        Surface(
                            color = CyberCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "ROUTER ADMIN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = CyberCyanLight
                            )
                        }
                    }
                    Text(
                        text = "Subnet 192.168.1.0/24 Management",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // GitHub Admin Profile Badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToAdmin() },
                    color = DarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (adminProfile.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = adminProfile.avatarUrl,
                                contentDescription = "Admin Avatar",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CyberCyan, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${adminProfile.login}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                            }
                            Text(
                                text = "OAuth Admin",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = EmeraldOnline
                            )
                        }
                    }
                }
            }
        }

        // Hero Banner with Network Image & Status Overlay
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_network),
                    contentDescription = "Network mesh hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkBackground.copy(alpha = 0.92f))
                            )
                        )
                )

                // Hero Content Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DarkBackground.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldOnline.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldOnline)
                                )
                                Text(
                                    text = "GATEWAY ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = EmeraldOnline
                                )
                            }
                        }

                        // Link Speed Chip
                        Surface(
                            color = DarkBackground.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${wifiInfo.linkSpeedMbps} Mbps Link",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CyberCyanLight
                            )
                        }
                    }

                    Column {
                        Text(
                            text = wifiInfo.ssid,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Gateway: ${wifiInfo.gatewayIp}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = CyberCyanLight
                            )
                            Text("•", color = TextMuted)
                            Text(
                                text = wifiInfo.frequencyGhz,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Live Scanning Progress indicator
        if (isScanning) {
            item {
                Surface(
                    color = CyberCyanDark.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { scanProgress / 100f },
                                    modifier = Modifier.size(18.dp),
                                    color = CyberCyanLight,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Discovering Subnet Devices...",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CyberCyanLight
                                )
                            }
                            Text(
                                text = "$scanProgress%",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                color = CyberCyanLight
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { scanProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyberCyanLight,
                            trackColor = DarkSurfaceElevated
                        )
                    }
                }
            }
        }

        // 4 Key Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Connected",
                        value = "${allDevices.size}",
                        subtitle = "Subnet nodes",
                        icon = Icons.Default.Devices,
                        iconTint = CyberCyanLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_connected"),
                        onClick = onNavigateToDevices
                    )
                    MetricStatCard(
                        title = "Active Online",
                        value = "$activeCount",
                        subtitle = "${if (allDevices.isNotEmpty()) (activeCount * 100 / allDevices.size) else 0}% online",
                        icon = Icons.Default.Wifi,
                        iconTint = EmeraldOnline,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_active"),
                        onClick = onNavigateToDevices
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Blocked",
                        value = "$blockedCount",
                        subtitle = "Firewall ACL rules",
                        icon = Icons.Default.Security,
                        iconTint = RoseBlocked,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_blocked"),
                        onClick = onNavigateToDevices
                    )
                    MetricStatCard(
                        title = "Throughput",
                        value = "%.1f".format(totalDownload),
                        subtitle = "↓ / %.1f ↑ Mbps".format(totalUpload),
                        icon = Icons.Default.Speed,
                        iconTint = IndigoLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_throughput")
                    )
                }
            }
        }

        // Quick Administrative Actions Row
        item {
            Text(
                text = "Quick Admin Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rescan Subnet
                Button(
                    onClick = { viewModel.startSubnetScan() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rescan_subnet_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberCyanLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Subnet", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }

                // Open Gateway Web Portal
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://${wifiInfo.gatewayIp}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            viewModel.showToast("Cannot launch browser for ${wifiInfo.gatewayIp}")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_gateway_portal_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Router Portal", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Launch React Dashboard
                Button(
                    onClick = onNavigateToWebDashboard,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("launch_react_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDark),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("React Web UI", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                }

                // Reboot Router
                Button(
                    onClick = { showRebootDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reboot_router_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseBlocked.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseBlocked.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = RoseBlocked, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reboot Router", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = RoseBlocked)
                }
            }
        }

        // Detailed WiFi Specifications Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Local Network Parameters",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = { viewModel.refreshWifi() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Refresh", tint = CyberCyan, modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = DarkSurfaceBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Local Device IP:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(wifiInfo.localIp, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = CyberCyanLight)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gateway Router IP:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(wifiInfo.gatewayIp, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = TextPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subnet Mask:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(wifiInfo.subnetMask, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = TextSecondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Router BSSID:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(wifiInfo.bssid, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = TextSecondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Signal Quality:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("${wifiInfo.signalPercent}% (${wifiInfo.rssiDbm} dBm)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = EmeraldOnline)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DNS Servers:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("${wifiInfo.dns1}, ${wifiInfo.dns2}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = TextSecondary)
                    }
                }
            }
        }

        // Recent Audit Log Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Security Audit Logs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToAdmin) {
                    Text("View All", color = CyberCyanLight, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        items(auditLogs.take(4)) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = log.timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = TextMuted
                    )
                    Text(
                        text = log.adminUser,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyanLight
                    )
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Router Reboot Dialog
    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Reboot Gateway Router?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This command will dispatch a hardware reset to router ${wifiInfo.gatewayIp}. All connected devices will temporarily disconnect for ~45 seconds. Admin action will be logged under @${adminProfile.login}.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rebootRouter()
                        showRebootDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseBlocked)
                ) {
                    Text("Reboot Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
