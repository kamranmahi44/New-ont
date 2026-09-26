package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AdminProfile
import com.example.model.WifiInfo
import com.example.network.GitHubAuthService
import com.example.network.NetworkDiscoveryManager
import com.example.network.WifiManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkRepository(
    private val context: Context,
    private val deviceDao: DeviceDao,
    private val adminLogDao: AdminLogDao
) {
    private val wifiHelper = WifiManagerHelper(context)
    val discoveryManager = NetworkDiscoveryManager(context, deviceDao, adminLogDao, wifiHelper)
    val gitHubAuthService = GitHubAuthService()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("net_admin_prefs", Context.MODE_PRIVATE)

    private val _adminProfile = MutableStateFlow(loadSavedAdminProfile())
    val adminProfile: StateFlow<AdminProfile> = _adminProfile.asStateFlow()

    private val _wifiInfo = MutableStateFlow(wifiHelper.getWifiInfo())
    val wifiInfo: StateFlow<WifiInfo> = _wifiInfo.asStateFlow()

    val isScanning: StateFlow<Boolean> = discoveryManager.isScanning
    val scanProgress: StateFlow<Int> = discoveryManager.scanProgress
    val lastScanTime: StateFlow<String> = discoveryManager.lastScanTime

    val devicesFlow: Flow<List<DeviceEntity>> = deviceDao.getAllDevicesFlow()
    val auditLogsFlow: Flow<List<AdminLogEntity>> = adminLogDao.getRecentLogsFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialTopologyIfNeeded()
            // Start periodic discovery scan every 45 seconds to detect new connected devices
            discoveryManager.startPeriodicDiscovery(intervalMs = 45_000L)
        }
    }

    private suspend fun seedInitialTopologyIfNeeded() {
        val currentInfo = wifiHelper.getWifiInfo()
        _wifiInfo.value = currentInfo

        logAction("System", "Initial subnet initialized for ${currentInfo.ssid} (${currentInfo.gatewayIp})")
        logAction(
            _adminProfile.value.login,
            "Admin session initialized via ${_adminProfile.value.authMethod}"
        )

        // Run initial discovery
        discoveryManager.runSubnetDiscoveryScan()
    }

    fun refreshWifiInfo() {
        _wifiInfo.value = wifiHelper.getWifiInfo()
    }

    suspend fun performSubnetScan() = withContext(Dispatchers.IO) {
        refreshWifiInfo()
        val scanned = discoveryManager.runSubnetDiscoveryScan()
        logAction(
            _adminProfile.value.login,
            "Manual subnet scan executed. ${scanned.size} nodes mapped."
        )
    }

    suspend fun toggleBlockDevice(ip: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        deviceDao.setBlocked(ip, isBlocked)
        val dev = deviceDao.getDeviceByIp(ip)
        val name = dev?.displayName ?: ip
        val action = if (isBlocked) "Blocked device $name ($ip) from local network" else "Restored access for $name ($ip)"
        logAction(_adminProfile.value.login, action, ip)
    }

    suspend fun setBandwidthLimit(ip: String, limitMbps: Int) = withContext(Dispatchers.IO) {
        deviceDao.setBandwidthLimit(ip, limitMbps)
        val dev = deviceDao.getDeviceByIp(ip)
        val name = dev?.displayName ?: ip
        val limitText = if (limitMbps == 0) "Unlimited" else "$limitMbps Mbps"
        logAction(_adminProfile.value.login, "Set bandwidth cap to $limitText for $name ($ip)", ip)
    }

    suspend fun setQosPriority(ip: String, priority: String) = withContext(Dispatchers.IO) {
        deviceDao.setQosPriority(ip, priority)
        val dev = deviceDao.getDeviceByIp(ip)
        val name = dev?.displayName ?: ip
        logAction(_adminProfile.value.login, "Updated QoS priority to $priority for $name ($ip)", ip)
    }

    suspend fun setDeviceAlias(ip: String, alias: String) = withContext(Dispatchers.IO) {
        deviceDao.setAlias(ip, alias)
        logAction(_adminProfile.value.login, "Renamed device $ip to '$alias'", ip)
    }

    suspend fun rebootRouter() = withContext(Dispatchers.IO) {
        val gw = _wifiInfo.value.gatewayIp
        logAction(_adminProfile.value.login, "DISPATCHED HARD REBOOT to Gateway Router ($gw)")
    }

    suspend fun logAction(user: String, action: String, targetIp: String? = null) = withContext(Dispatchers.IO) {
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        adminLogDao.insertLog(
            AdminLogEntity(
                timestamp = System.currentTimeMillis(),
                timeFormatted = timeFormatted,
                adminUser = "@${user.removePrefix("@")}",
                action = action,
                targetIp = targetIp
            )
        )
    }

    suspend fun setAdminProfile(profile: AdminProfile) = withContext(Dispatchers.IO) {
        _adminProfile.value = profile
        prefs.edit()
            .putString("admin_login", profile.login)
            .putString("admin_name", profile.name)
            .putString("admin_avatar", profile.avatarUrl)
            .putString("admin_bio", profile.bio)
            .putString("admin_role", profile.role)
            .putString("admin_token", profile.token)
            .putBoolean("admin_is_auth", profile.isAuthenticated)
            .putString("admin_auth_method", profile.authMethod)
            .apply()

        logAction(profile.login, "Admin Authenticated via ${profile.authMethod}")
    }

    suspend fun logoutAdmin() = withContext(Dispatchers.IO) {
        val currentLogin = _adminProfile.value.login
        val unauthenticated = AdminProfile(
            login = "guest_admin",
            name = "Guest Admin",
            avatarUrl = "",
            bio = "Subnet local operator",
            role = "Operator",
            token = "",
            isAuthenticated = false,
            authMethod = "None"
        )
        _adminProfile.value = unauthenticated
        prefs.edit().clear().apply()
        logAction(currentLogin, "Admin signed out")
    }

    private fun loadSavedAdminProfile(): AdminProfile {
        val isAuth = prefs.getBoolean("admin_is_auth", true)
        if (!isAuth) {
            return AdminProfile(
                login = "guest",
                name = "Local Admin",
                avatarUrl = "",
                bio = "Unauthenticated local admin",
                role = "Local Operator",
                token = "",
                isAuthenticated = false,
                authMethod = "None"
            )
        }
        return AdminProfile(
            login = prefs.getString("admin_login", "octocat") ?: "octocat",
            name = prefs.getString("admin_name", "Monalisa Octocat") ?: "Monalisa Octocat",
            avatarUrl = prefs.getString("admin_avatar", "https://avatars.githubusercontent.com/u/583231?v=4")
                ?: "https://avatars.githubusercontent.com/u/583231?v=4",
            bio = prefs.getString("admin_bio", "Subnet Network Operations Lead & Core Maintainer")
                ?: "Subnet Network Operations Lead & Core Maintainer",
            publicRepos = 8,
            followers = 1337,
            role = prefs.getString("admin_role", "Verified GitHub SuperAdmin") ?: "Verified GitHub SuperAdmin",
            token = prefs.getString("admin_token", "gho_octocat_demo_session") ?: "gho_octocat_demo_session",
            isAuthenticated = true,
            authMethod = prefs.getString("admin_auth_method", "GitHub OAuth 2.0") ?: "GitHub OAuth 2.0"
        )
    }
}
