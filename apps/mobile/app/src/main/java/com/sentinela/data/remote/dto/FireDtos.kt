package com.sentinela.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FiresResponseDto(
    val count: Int,
    val bbox: String,
    val days: Int,
    val source: String,
    val hotspots: List<FireHotspotDto>,
)

@Serializable
data class FireHotspotDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val acquiredAt: String,
    val satellite: String,
    val instrument: String,
    val confidence: String,
    val frp: Double? = null,
    val dayNight: String,
)

@Serializable
data class RiskResponseDto(
    val bbox: String,
    val days: Int,
    val risk: RegionRiskDto,
)

@Serializable
data class RegionRiskDto(
    val level: String,
    val score: Double,
    val totalFires: Int,
    val trend: String,
)
