package com.sentinela.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sentinela.domain.model.MonitoredPoint

@Entity(tableName = "monitored_points")
data class MonitoredPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val inAlert: Boolean = false,
)

fun MonitoredPointEntity.toDomain(): MonitoredPoint = MonitoredPoint(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    radiusKm = radiusKm,
    inAlert = inAlert,
)

fun MonitoredPoint.toEntity(): MonitoredPointEntity = MonitoredPointEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    radiusKm = radiusKm,
    inAlert = inAlert,
)
