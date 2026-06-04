package com.sentinela.domain.model

data class MonitoredPoint(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val inAlert: Boolean = false,
)
