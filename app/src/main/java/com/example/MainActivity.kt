package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private var viewModelInstance: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            viewModelInstance = viewModel

            // Handle incoming OAuth intent if present on launch
            LaunchedEffect(intent) {
                handleOAuthIntent(intent, viewModel)
            }

            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModelInstance?.let { vm ->
            handleOAuthIntent(intent, vm)
        }
    }

    private fun handleOAuthIntent(intent: Intent?, vm: MainViewModel) {
        val data = intent?.data
        if (data != null && data.scheme == "netadmin" && data.host == "oauth-callback") {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                vm.showToast("GitHub OAuth code received: ${code.take(8)}... Authenticating")
                vm.loginWithGitHubToken(code)
            }
        }
    }
}

enum class NavigationTab(val label: String) {
    DASHBOARD("Dashboard"),
    DEVICES("Devices"),
    WEB_DASHBOARD("Web App"),
    ADMIN("Admin")
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Handle back button to return to Dashboard
    if (selectedTab != NavigationTab.DASHBOARD) {
        BackHandler {
            selectedTab = NavigationTab.DASHBOARD
        }
    }

    // Display snackbar toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Text(data.visuals.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.DASHBOARD,
                    onClick = { selectedTab = NavigationTab.DASHBOARD },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavigationTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.DASHBOARD.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyanLight,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // Tab 2: Devices
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.DEVICES,
                    onClick = { selectedTab = NavigationTab.DEVICES },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavigationTab.DEVICES) Icons.Filled.Devices else Icons.Outlined.Devices,
                            contentDescription = "Devices"
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.DEVICES.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyanLight,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_devices")
                )

                // Tab 3: Web Dashboard (React + Firebase Hosting)
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.WEB_DASHBOARD,
                    onClick = { selectedTab = NavigationTab.WEB_DASHBOARD },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavigationTab.WEB_DASHBOARD) Icons.Filled.Web else Icons.Outlined.Web,
                            contentDescription = "Web Dashboard"
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.WEB_DASHBOARD.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyanLight,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_web_dashboard")
                )

                // Tab 4: Admin & GitHub
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.ADMIN,
                    onClick = { selectedTab = NavigationTab.ADMIN },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavigationTab.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Admin"
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.ADMIN.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyanLight,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDevices = { selectedTab = NavigationTab.DEVICES },
                    onNavigateToWebDashboard = { selectedTab = NavigationTab.WEB_DASHBOARD },
                    onNavigateToAdmin = { selectedTab = NavigationTab.ADMIN }
                )
                NavigationTab.DEVICES -> DevicesScreen(
                    viewModel = viewModel
                )
                NavigationTab.WEB_DASHBOARD -> WebDashboardScreen(
                    viewModel = viewModel
                )
                NavigationTab.ADMIN -> AdminAuthScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
