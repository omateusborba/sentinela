package com.sentinela.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.di.FiresSessionStore
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.util.haversineKm
import com.sentinela.ui.components.LoadableUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AlertItem(
    val point: MonitoredPoint,
    val distanceKm: Double,
    val fire: FireHotspot,
)

class AlertViewModel(
    pointRepository: MonitoredPointRepository,
    firesSessionStore: FiresSessionStore,
) : ViewModel() {

    val uiState: StateFlow<LoadableUiState<List<AlertItem>>> = combine(
        pointRepository.observeAll(),
    ) { pointsArray ->
        val points = pointsArray[0]
        val fires = firesSessionStore.fires
        val alerts = buildAlerts(points, fires)
        if (alerts.isEmpty()) {
            LoadableUiState.Success(emptyList())
        } else {
            LoadableUiState.Success(alerts)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LoadableUiState.Success(emptyList()),
    )

    private fun buildAlerts(
        points: List<MonitoredPoint>,
        fires: List<FireHotspot>,
    ): List<AlertItem> {
        val result = mutableListOf<AlertItem>()
        points.filter { it.inAlert }.forEach { point ->
            fires.map { fire ->
                val distance = haversineKm(
                    point.latitude,
                    point.longitude,
                    fire.latitude,
                    fire.longitude,
                )
                fire to distance
            }
                .filter { (_, d) -> d <= point.radiusKm }
                .minByOrNull { (_, d) -> d }
                ?.let { (fire, distance) ->
                    result.add(AlertItem(point, distance, fire))
                }
        }
        return result.sortedBy { it.distanceKm }
    }
}
