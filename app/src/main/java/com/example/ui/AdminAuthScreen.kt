package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AdminProfile
import com.example.ui.theme.*

@Composable
fun AdminAuthScreen(
    viewModel: MainViewModel
) {
    val adminProfile by viewModel.adminProfile.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val context = LocalContext.current
    var tokenInput by remember { mutableStateOf("") }
    var showTokenDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_auth_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Administrator Access",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "GitHub OAuth 2.0 Network Administrator Authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Active Admin Profile Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (adminProfile.isAuthenticated) CyberCyan.copy(alpha = 0.5f) else DarkSurfaceBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (adminProfile.isAuthenticated) EmeraldOnline.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (adminProfile.isAuthenticated) EmeraldOnline.copy(alpha = 0.4f) else AmberWarning.copy(alpha = 0.4f)
                            )
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
                                        .background(if (adminProfile.isAuthenticated) EmeraldOnline else AmberWarning)
                                )
                                Text(
                                    text = if (adminProfile.isAuthenticated) "VERIFIED SUPERADMIN" else "GUEST OPERATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = if (adminProfile.isAuthenticated) EmeraldOnline else AmberWarning
                                )
                            }
                        }

                        Text(
                            text = adminProfile.authMethod,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    // Avatar + Profile details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (adminProfile.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = adminProfile.avatarUrl,
                                contentDescription = "Admin Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, CyberCyan, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = adminProfile.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${adminProfile.login}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CyberCyanLight
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = adminProfile.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = DarkSurfaceBorder)

                    // Admin Capabilities Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Admin Permissions Granted:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )

                        val perms = listOf(
                            "Subnet Device Firewall (Block/Allow ACL)",
                            "Per-Device Bandwidth Throttling & QoS Shaper",
                            "Gateway Router Hardware Power/Reboot Control",
                            "Local ARP & ICMP Subnet Discovery",
                            "Security Audit Log Authority"
                        )

                        perms.forEach { perm ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(14.dp))
                                Text(perm, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }

                    // Logout Button
                    if (adminProfile.isAuthenticated) {
                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logout_admin_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseBlocked.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseBlocked)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out from Admin Session")
                        }
                    }
                }
            }
        }

        // Authentication Methods Section
        item {
            Text(
                text = "Authenticate / Switch Admin",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // 1-Click Demo Octocat Admin Login
        item {
            Button(
                onClick = { viewModel.loginAsOctocatDemo() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_admin_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = CyberCyanLight)
                        Column {
                            Text("Sign in as @octocat (Demo Admin)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Instant 1-tap SuperAdmin verification", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }
        }

        // Token Input Button
        item {
            Button(
                onClick = { showTokenDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pat_token_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndigoLight)
                        Column {
                            Text("GitHub Personal Access Token (PAT)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Fetch real profile from api.github.com", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }
        }

        // Web OAuth Flow Button
        item {
            Button(
                onClick = {
                    try {
                        val authUrl = viewModel.repository.gitHubAuthService.authorizationUrl
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        viewModel.showToast("Cannot launch browser. Using Demo Octocat authentication.")
                        viewModel.loginAsOctocatDemo()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_oauth_web_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign In with GitHub OAuth 2.0",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Full Security Audit Log List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Administrator Action History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${auditLogs.size} Events",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        items(auditLogs) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = log.timeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextMuted
                            )
                            Text(
                                text = log.adminUser,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyberCyanLight
                            )
                        }

                        Surface(
                            color = DarkBackground,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LOGGED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = EmeraldOnline
                            )
                        }
                    }

                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    if (log.targetIp != null) {
                        Text(
                            text = "Target IP: ${log.targetIp}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = IndigoLight
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Token Input Dialog
    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Enter GitHub Access Token", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Paste a GitHub Personal Access Token (classic or fine-grained) with 'read:user' scope.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        placeholder = { Text("ghp_xxxx or gho_xxxx", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tokenInput.isNotBlank()) {
                            viewModel.loginWithGitHubToken(tokenInput)
                            showTokenDialog = false
                            tokenInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanDark)
                ) {
                    Text("Authenticate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
