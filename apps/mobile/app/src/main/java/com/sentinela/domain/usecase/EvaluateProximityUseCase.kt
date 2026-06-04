package com.sentinela.domain.usecase

import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.util.haversineKm

class EvaluateProximityUseCase {

    operator fun invoke(
        points: List<MonitoredPoint>,
        fires: List<FireHotspot>,
    ): ProximityEvaluationResult {
        val newAlerts = mutableListOf<ProximityAlert>()
        val updated = points.map { point ->
            var minDistance: Double? = null
            var closestFireId: String? = null
            var insideRadius = false

            for (fire in fires) {
                val distance = haversineKm(
                    point.latitude,
                    point.longitude,
                    fire.latitude,
                    fire.longitude,
                )
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance
                    closestFireId = fire.id
                }
                if (distance <= point.radiusKm) {
                    insideRadius = true
                }
            }

            val wasInAlert = point.inAlert
            val updatedPoint = point.copy(inAlert = insideRadius)

            if (insideRadius && !wasInAlert && minDistance != null && closestFireId != null) {
                newAlerts.add(
                    ProximityAlert(
                        pointId = point.id,
                        pointName = point.name,
                        distanceKm = minDistance,
                        fireId = closestFireId,
                    ),
                )
            }

            updatedPoint
        }

        return ProximityEvaluationResult(
            updatedPoints = updated,
            newAlerts = newAlerts,
        )
    }
}
