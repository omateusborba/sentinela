package com.sentinela.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MonitoredPointEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monitoredPointDao(): MonitoredPointDao
}
