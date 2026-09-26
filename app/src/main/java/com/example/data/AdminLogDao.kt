package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogsFlow(): Flow<List<AdminLogEntity>>

    @Insert
    suspend fun insertLog(log: AdminLogEntity)

    @Query("DELETE FROM admin_audit_logs")
    suspend fun clearLogs()
}
