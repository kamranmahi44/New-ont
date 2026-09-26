package com.example.network

import android.webkit.JavascriptInterface
import com.example.data.DeviceEntity
import com.example.data.NetworkRepository
import com.example.model.AdminProfile
import com.example.model.WifiInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WebDashboardBridge(
    private val repository: NetworkRepository,
    private val scope: CoroutineScope,
    private val onToastMessage: (String) -> Unit
) {

    @JavascriptInterface
    fun onDashboardReady() {
        scope.launch(Dispatchers.Main) {
            onToastMessage("React Web Dashboard connected to Android Core")
        }
    }

    @JavascriptInterface
    fun toggleBlockDevice(ip: String, isBlocked: Boolean) {
        scope.launch {
            repository.toggleBlockDevice(ip, isBlocked)
        }
    }

    @JavascriptInterface
    fun setBandwidthLimit(ip: String, limitMbps: Int) {
        scope.launch {
            repository.setBandwidthLimit(ip, limitMbps)
        }
    }

    @JavascriptInterface
    fun setQosPriority(ip: String, priority: String) {
        scope.launch {
            repository.setQosPriority(ip, priority)
        }
    }

    @JavascriptInterface
    fun setDeviceAlias(ip: String, alias: String) {
        scope.launch {
            repository.setDeviceAlias(ip, alias)
        }
    }

    @JavascriptInterface
    fun rescanSubnet() {
        scope.launch {
            repository.performSubnetScan()
        }
    }

    @JavascriptInterface
    fun rebootRouter() {
        scope.launch {
            repository.rebootRouter()
        }
    }

    companion object {
        fun devicesToJson(devices: List<DeviceEntity>): String {
            val array = JSONArray()
            for (d in devices) {
                val obj = JSONObject()
                obj.put("id", d.ip)
                obj.put("name", d.name)
                obj.put("alias", d.alias)
                obj.put("ip", d.ip)
                obj.put("mac", d.mac)
                obj.put("vendor", d.vendor)
                obj.put("deviceType", d.deviceType)
                obj.put("isBlocked", d.isBlocked)
                obj.put("bandwidthLimitMbps", d.bandwidthLimitMbps)
                obj.put("qosPriority", d.qosPriority)
                obj.put("pingMs", d.pingMs)
                obj.put("isOnline", d.isOnline)
                obj.put("downloadSpeedMbps", d.downloadSpeedMbps)
                obj.put("uploadSpeedMbps", d.uploadSpeedMbps)
                obj.put("firstSeen", d.firstSeen)
                obj.put("lastSeen", d.lastSeen)

                val portsArray = JSONArray()
                for (port in d.openPortsList) {
                    portsArray.put(port)
                }
                obj.put("openPorts", portsArray)
                array.put(obj)
            }
            return array.toString()
        }

        fun wifiInfoToJson(wifi: WifiInfo): String {
            val obj = JSONObject()
            obj.put("ssid", wifi.ssid)
            obj.put("bssid", wifi.bssid)
            obj.put("ipAddress", wifi.localIp)
            obj.put("gateway", wifi.gatewayIp)
            obj.put("subnetMask", wifi.subnetMask)
            obj.put("linkSpeed", wifi.linkSpeedMbps)
            obj.put("frequency", wifi.frequencyGhz)
            obj.put("signalStrengthDbm", wifi.rssiDbm)
            obj.put("signalPercent", wifi.signalPercent)
            obj.put("security", wifi.securityType)
            return obj.toString()
        }

        fun adminToJson(admin: AdminProfile): String {
            val obj = JSONObject()
            obj.put("login", admin.login)
            obj.put("name", admin.name)
            obj.put("avatarUrl", admin.avatarUrl)
            obj.put("bio", admin.bio)
            obj.put("role", admin.role)
            obj.put("authenticated", admin.isAuthenticated)
            obj.put("authMethod", admin.authMethod)
            return obj.toString()
        }
    }
}
