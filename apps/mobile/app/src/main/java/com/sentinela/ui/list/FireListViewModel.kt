package com.sentinela.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.di.FiresSessionStore
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.util.haversineKm
import com.sentinela.ui.components.LoadableUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
enum class FireSortMode { RECENCY, PROXIMITY }

data class FireListItem(
    val fire: FireHotspot,
    val nearestPointName: String?,
    val nearestDistanceKm: Double?,
)

data class FireListSuccess(
    val items: List<FireListItem>,
    val sortMode: FireSortMode,
)

class FireListViewModel(
    private val firesSessionStore: FiresSessionStore,
    private val pointRepository: MonitoredPointRepository,
) : ViewModel() {

    private val _sortMode = MutableStateFlow(FireSortMode.RECENCY)
    val sortMode: StateFlow<FireSortMode> = _sortMode.asStateFlow()

    private val points = pointRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<LoadableUiState<FireListSuccess>> = combine(
        _sortMode,
        points,
    ) { sort, pts ->
        buildState(sort, pts)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        buildState(FireSortMode.RECENCY, emptyList()),
    )

    fun setSortMode(mode: FireSortMode) {
        _sortMode.value = mode
    }

    private fun buildState(
        sort: FireSortMode,
        monitoredPoints: List<MonitoredPoint>,
    ): LoadableUiState<FireListSuccess> {
        val fires = firesSessionStore.fires
        if (fires.isEmpty()) {
            return LoadableUiState.Error("Nenhum foco carregado. Abra o mapa e atualize.")
        }

        val items = fires.map { fire ->
            var nearestName: String? = null
            var nearestKm: Double? = null
            monitoredPoints.forEach { point ->
                val d = haversineKm(
                    point.latitude,
                    point.longitude,
                    fire.latitude,
                    fire.longitude,
                )
                if (nearestKm == null || d < nearestKm) {
                    nearestKm = d
                    nearestName = point.name
                }
            }
            FireListItem(fire, nearestName, nearestKm)
        }

        val sorted = when (sort) {
            FireSortMode.RECENCY -> items.sortedByDescending { it.fire.acquiredAt }
            FireSortMode.PROXIMITY -> items.sortedBy { it.nearestDistanceKm ?: Double.MAX_VALUE }
        }

        return LoadableUiState.Success(FireListSuccess(sorted, sort))
    }
}
