package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminLogEntity
import com.example.data.AppDatabase
import com.example.data.DeviceEntity
import com.example.data.NetworkRepository
import com.example.model.AdminProfile
import com.example.model.WifiInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = NetworkRepository(
        context = application,
        deviceDao = database.deviceDao(),
        adminLogDao = database.adminLogDao()
    )

    val wifiInfo: StateFlow<WifiInfo> = repository.wifiInfo
    val adminProfile: StateFlow<AdminProfile> = repository.adminProfile
    val isScanning: StateFlow<Boolean> = repository.isScanning
    val scanProgress: StateFlow<Int> = repository.scanProgress

    val allDevices: StateFlow<List<DeviceEntity>> = repository.devicesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val auditLogs: StateFlow<List<AdminLogEntity>> = repository.auditLogsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("all") // all, online, blocked, laptop, mobile, iot, media

    val filteredDevices: StateFlow<List<DeviceEntity>> = combine(
        allDevices,
        searchQuery,
        selectedFilter
    ) { devices, query, filter ->
        devices.filter { dev ->
            val matchesQuery = query.isBlank() ||
                    dev.name.contains(query, ignoreCase = true) ||
                    dev.alias.contains(query, ignoreCase = true) ||
                    dev.ip.contains(query) ||
                    dev.mac.contains(query, ignoreCase = true) ||
                    dev.vendor.contains(query, ignoreCase = true)

            if (!matchesQuery) return@filter false

            when (filter) {
                "online" -> dev.isOnline && !dev.isBlocked
                "blocked" -> dev.isBlocked
                "laptop" -> dev.deviceType == "laptop"
                "mobile" -> dev.deviceType == "mobile"
                "iot" -> dev.deviceType == "iot"
                "media" -> dev.deviceType == "media" || dev.deviceType == "gaming"
                else -> true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDeviceForInspect = MutableStateFlow<DeviceEntity?>(null)
    val selectedDeviceForInspect: StateFlow<DeviceEntity?> = _selectedDeviceForInspect.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setInspectDevice(device: DeviceEntity?) {
        _selectedDeviceForInspect.value = device
    }

    fun refreshWifi() {
        repository.refreshWifiInfo()
        showToast("WiFi telemetry refreshed")
    }

    fun startSubnetScan() {
        viewModelScope.launch {
            showToast("Scanning local subnet ${wifiInfo.value.gatewayIp.substringBeforeLast(".")}.0/24...")
            repository.performSubnetScan()
            showToast("Subnet discovery complete")
        }
    }

    fun toggleBlock(device: DeviceEntity) {
        viewModelScope.launch {
            val newBlocked = !device.isBlocked
            repository.toggleBlockDevice(device.ip, newBlocked)
            showToast(if (newBlocked) "Blocked ${device.displayName} (${device.ip})" else "Allowed ${device.displayName}")
        }
    }

    fun setBandwidthLimit(device: DeviceEntity, limitMbps: Int) {
        viewModelScope.launch {
            repository.setBandwidthLimit(device.ip, limitMbps)
            val text = if (limitMbps == 0) "Unlimited" else "$limitMbps Mbps"
            showToast("Set bandwidth limit: $text for ${device.displayName}")
        }
    }

    fun setQosPriority(device: DeviceEntity, priority: String) {
        viewModelScope.launch {
            repository.setQosPriority(device.ip, priority)
            showToast("Set QoS to $priority for ${device.displayName}")
        }
    }

    fun renameDevice(device: DeviceEntity, newAlias: String) {
        viewModelScope.launch {
            repository.setDeviceAlias(device.ip, newAlias)
            showToast("Device renamed to '$newAlias'")
        }
    }

    fun rebootRouter() {
        viewModelScope.launch {
            repository.rebootRouter()
            showToast("Reboot command sent to Gateway (${wifiInfo.value.gatewayIp})")
        }
    }

    fun sendWakeOnLan(device: DeviceEntity) {
        viewModelScope.launch {
            repository.logAction(adminProfile.value.login, "Sent Wake-on-LAN magic packet to ${device.mac}", device.ip)
            showToast("Wake-on-LAN packet transmitted to ${device.mac}")
        }
    }

    fun loginWithGitHubToken(token: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val result = repository.gitHubAuthService.authenticateWithToken(token)
            result.onSuccess { profile ->
                repository.setAdminProfile(profile)
                showToast("Welcome @${profile.login} - Admin Verified!")
            }.onFailure { err ->
                showToast("GitHub Auth failed: ${err.message ?: "Invalid Token"}")
            }
            _isAuthenticating.value = false
        }
    }

    fun loginAsOctocatDemo() {
        viewModelScope.launch {
            val demo = repository.gitHubAuthService.getDemoOctocatAdmin()
            repository.setAdminProfile(demo)
            showToast("Logged in as @${demo.login} (Admin Verified)")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAdmin()
            showToast("Logged out from Admin session")
        }
    }
}
