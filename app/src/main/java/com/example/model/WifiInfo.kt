package com.example.model

data class WifiInfo(
    val isConnected: Boolean = true,
    val ssid: String = "Local Network (WiFi)",
    val bssid: String = "74:83:C2:9A:F1:40",
    val localIp: String = "192.168.1.108",
    val gatewayIp: String = "192.168.1.1",
    val subnetMask: String = "255.255.255.0",
    val linkSpeedMbps: Int = 866,
    val frequencyGhz: String = "5.0 GHz (Channel 36)",
    val rssiDbm: Int = -52,
    val signalPercent: Int = 92,
    val securityType: String = "WPA2/WPA3 Personal",
    val dns1: String = "1.1.1.1",
    val dns2: String = "8.8.8.8"
) {
    val isSignalExcellent: Boolean get() = signalPercent >= 75
}
