package com.sentinela.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredPointDao {

    @Query("SELECT * FROM monitored_points ORDER BY name ASC")
    fun getAll(): Flow<List<MonitoredPointEntity>>

    @Query("SELECT * FROM monitored_points WHERE id = :id")
    suspend fun getById(id: Long): MonitoredPointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MonitoredPointEntity): Long

    @Update
    suspend fun update(entity: MonitoredPointEntity)

    @Delete
    suspend fun delete(entity: MonitoredPointEntity)

    @Query("UPDATE monitored_points SET inAlert = :inAlert WHERE id = :id")
    suspend fun updateAlertState(id: Long, inAlert: Boolean)
}
