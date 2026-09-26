package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM connected_devices ORDER BY CASE WHEN ip LIKE '%.1' THEN 0 ELSE 1 END, ip ASC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM connected_devices WHERE ip = :ip LIMIT 1")
    suspend fun getDeviceByIp(ip: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("UPDATE connected_devices SET isBlocked = :isBlocked WHERE ip = :ip")
    suspend fun setBlocked(ip: String, isBlocked: Boolean)

    @Query("UPDATE connected_devices SET bandwidthLimitMbps = :limitMbps WHERE ip = :ip")
    suspend fun setBandwidthLimit(ip: String, limitMbps: Int)

    @Query("UPDATE connected_devices SET qosPriority = :priority WHERE ip = :ip")
    suspend fun setQosPriority(ip: String, priority: String)

    @Query("UPDATE connected_devices SET alias = :alias WHERE ip = :ip")
    suspend fun setAlias(ip: String, alias: String)

    @Query("UPDATE connected_devices SET isOnline = :isOnline, lastSeen = :lastSeen WHERE ip = :ip")
    suspend fun setOnlineStatus(ip: String, isOnline: Boolean, lastSeen: String)

    @Query("DELETE FROM connected_devices WHERE ip = :ip")
    suspend fun deleteDevice(ip: String)

    @Query("DELETE FROM connected_devices")
    suspend fun clearAll()
}
