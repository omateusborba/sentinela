package com.sentinela.data.remote

import com.sentinela.data.remote.dto.FireHotspotDto
import com.sentinela.data.remote.dto.RegionRiskDto
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.RegionRisk
import com.sentinela.domain.model.RiskLevel

fun FireHotspotDto.toDomain(): FireHotspot = FireHotspot(
    id = id,
    latitude = latitude,
    longitude = longitude,
    acquiredAt = acquiredAt,
    satellite = satellite,
    instrument = instrument,
    confidence = confidence,
    frp = frp,
    dayNight = dayNight,
)

fun RegionRiskDto.toDomain(): RegionRisk = RegionRisk(
    level = when (level.uppercase()) {
        "LOW" -> RiskLevel.LOW
        "HIGH" -> RiskLevel.HIGH
        else -> RiskLevel.MEDIUM
    },
    score = score,
    totalFires = totalFires,
    trend = trend,
)
