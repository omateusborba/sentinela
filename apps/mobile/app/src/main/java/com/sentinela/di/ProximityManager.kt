package com.sentinela.di

import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.usecase.EvaluateProximityUseCase
import com.sentinela.notification.NotificationHelper

class ProximityManager(
    private val evaluateProximityUseCase: EvaluateProximityUseCase,
    private val pointRepository: MonitoredPointRepository,
    private val notificationHelper: NotificationHelper,
) {
    suspend fun evaluateAndNotify(
        points: List<MonitoredPoint>,
        fires: List<FireHotspot>,
    ): List<MonitoredPoint> {
        val result = evaluateProximityUseCase(points, fires)
        pointRepository.updateAlertStates(result.updatedPoints)
        result.newAlerts.forEach { notificationHelper.showProximityAlert(it) }
        return result.updatedPoints
    }
}
