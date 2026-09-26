package com.example.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.network.WebDashboardBridge
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebDashboardScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Source switcher: "local" (assets) vs "firebase" (Firebase Hosting)
    var isFirebaseHostingMode by remember { mutableStateOf(false) }
    var firebaseHostingUrl by remember { mutableStateOf("https://netadmin-portal.web.app") }
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrlInput by remember { mutableStateOf(firebaseHostingUrl) }

    val allDevices by viewModel.allDevices.collectAsState()
    val wifiInfo by viewModel.wifiInfo.collectAsState()
    val adminProfile by viewModel.adminProfile.collectAsState()

    val currentUrl = if (isFirebaseHostingMode) firebaseHostingUrl else "file:///android_asset/connected_devices_dashboard.html"

    // Sync state changes from native Kotlin into the loaded React dashboard
    LaunchedEffect(allDevices) {
        if (allDevices.isNotEmpty() && webViewRef != null) {
            val json = WebDashboardBridge.devicesToJson(allDevices)
            webViewRef?.evaluateJavascript("if (window.updateDevices) { window.updateDevices($json); }", null)
        }
    }

    LaunchedEffect(wifiInfo) {
        if (webViewRef != null) {
            val json = WebDashboardBridge.wifiInfoToJson(wifiInfo)
            webViewRef?.evaluateJavascript("if (window.updateWifiInfo) { window.updateWifiInfo($json); }", null)
        }
    }

    LaunchedEffect(adminProfile) {
        if (webViewRef != null) {
            val json = WebDashboardBridge.adminToJson(adminProfile)
            webViewRef?.evaluateJavascript("if (window.updateAdminUser) { window.updateAdminUser($json); }", null)
        }
    }

    LaunchedEffect(currentUrl) {
        isLoading = true
        webViewRef?.loadUrl(currentUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("web_dashboard_screen")
    ) {
        // Top Web Control Header
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (isFirebaseHostingMode) Color(0xFFFFCA28).copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFirebaseHostingMode) Color(0xFFFFCA28).copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = if (isFirebaseHostingMode) "FIREBASE HOSTING" else "REACT ASSET",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = if (isFirebaseHostingMode) Color(0xFFFFCA28) else CyberCyanLight
                            )
                        }

                        Text(
                            text = if (isFirebaseHostingMode) "Cloud React Dashboard" else "In-App React Dashboard",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Switch between local asset and Firebase Hosting URL
                        IconButton(
                            onClick = {
                                isFirebaseHostingMode = !isFirebaseHostingMode
                                viewModel.showToast(
                                    if (isFirebaseHostingMode) "Switched to Firebase Hosting ($firebaseHostingUrl)"
                                    else "Switched to In-App Local Asset"
                                )
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isFirebaseHostingMode) Icons.Default.Cloud else Icons.Default.Storage,
                                contentDescription = "Toggle Source",
                                tint = if (isFirebaseHostingMode) Color(0xFFFFCA28) else CyberCyanLight,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Configure Firebase Hosting URL
                        if (isFirebaseHostingMode) {
                            IconButton(
                                onClick = {
                                    tempUrlInput = firebaseHostingUrl
                                    showUrlDialog = true
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit URL", tint = TextSecondary, modifier = Modifier.size(17.dp))
                            }
                        }

                        // Reload button
                        IconButton(
                            onClick = {
                                webViewRef?.reload()
                                viewModel.showToast("Reloaded React Dashboard")
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CyberCyanLight, modifier = Modifier.size(19.dp))
                        }

                        // Open external browser button
                        IconButton(
                            onClick = {
                                try {
                                    val uri = if (isFirebaseHostingMode) {
                                        Uri.parse(firebaseHostingUrl)
                                    } else {
                                        Uri.parse("file:///android_asset/connected_devices_dashboard.html")
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    viewModel.showToast("Cannot launch external browser")
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = "Open Browser", tint = TextSecondary, modifier = Modifier.size(19.dp))
                        }
                    }
                }

                // Current Source URL Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFirebaseHostingMode) firebaseHostingUrl else "file:///android_asset/connected_devices_dashboard.html",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = TextMuted,
                        maxLines = 1
                    )
                    Text(
                        text = "JS Bridge Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = EmeraldOnline
                    )
                }
            }
        }

        // Web Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = CyberCyan,
                trackColor = DarkBackground
            )
        }

        // WebView Embed
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    // Attach JavascriptInterface Bridge
                    val bridge = WebDashboardBridge(
                        repository = viewModel.repository,
                        scope = coroutineScope,
                        onToastMessage = { msg ->
                            viewModel.showToast(msg)
                        }
                    )
                    addJavascriptInterface(bridge, "AndroidBridge")

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false

                            // Inject initial current state into the React dashboard
                            val devicesJson = WebDashboardBridge.devicesToJson(allDevices)
                            val wifiJson = WebDashboardBridge.wifiInfoToJson(wifiInfo)
                            val adminJson = WebDashboardBridge.adminToJson(adminProfile)

                            view?.evaluateJavascript("""
                                if (window.updateWifiInfo) { window.updateWifiInfo($wifiJson); }
                                if (window.updateAdminUser) { window.updateAdminUser($adminJson); }
                                if (window.updateDevices) { window.updateDevices($devicesJson); }
                            """.trimIndent(), null)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            isLoading = false
                            // If Firebase Hosting URL isn't deployed yet or unreachable, automatically fallback to local asset!
                            if (isFirebaseHostingMode) {
                                viewModel.showToast("Remote URL unavailable. Falling back to local responsive React dashboard.")
                                isFirebaseHostingMode = false
                            }
                        }
                    }

                    loadUrl(currentUrl)
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            }
        )
    }

    // Custom Firebase Hosting URL Dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Firebase Hosting Domain", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the URL where your connected devices dashboard is hosted on Firebase Hosting (e.g. https://<project-id>.web.app):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = tempUrlInput,
                        onValueChange = { tempUrlInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFCA28),
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
                        if (tempUrlInput.isNotBlank()) {
                            firebaseHostingUrl = tempUrlInput.trim()
                            showUrlDialog = false
                            viewModel.showToast("Updated Firebase Hosting URL")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCA28), contentColor = DarkBackground)
                ) {
                    Text("Apply URL", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
