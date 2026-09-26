package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_audit_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = "",
    val adminUser: String = "Admin",
    val action: String = "",
    val targetIp: String? = null
)
