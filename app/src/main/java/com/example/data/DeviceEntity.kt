package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connected_devices")
data class DeviceEntity(
    @PrimaryKey
    val ip: String,
    val mac: String = "00:00:00:00:00:00",
    val name: String = "Unknown Device",
    val alias: String = "",
    val vendor: String = "Generic Hardware",
    val deviceType: String = "other", // router, laptop, mobile, iot, media, gaming, other
    val isBlocked: Boolean = false,
    val bandwidthLimitMbps: Int = 0, // 0 = unlimited
    val qosPriority: String = "Normal", // Low, Normal, High
    val pingMs: Long = 0,
    val isOnline: Boolean = true,
    val downloadSpeedMbps: Float = 0f,
    val uploadSpeedMbps: Float = 0f,
    val openPortsString: String = "",
    val firstSeen: String = "",
    val lastSeen: String = ""
) {
    val displayName: String
        get() = alias.ifBlank { name }

    val openPortsList: List<Int>
        get() = if (openPortsString.isBlank()) emptyList() else openPortsString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
}
