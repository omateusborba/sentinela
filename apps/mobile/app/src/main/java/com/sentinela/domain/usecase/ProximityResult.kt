package com.sentinela.domain.usecase

import com.sentinela.domain.model.MonitoredPoint

data class ProximityAlert(
    val pointId: Long,
    val pointName: String,
    val distanceKm: Double,
    val fireId: String,
)

data class ProximityEvaluationResult(
    val updatedPoints: List<MonitoredPoint>,
    val newAlerts: List<ProximityAlert>,
)
