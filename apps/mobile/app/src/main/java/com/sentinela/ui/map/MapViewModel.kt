package com.sentinela.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.data.MonitorRegions
import com.sentinela.di.FiresSessionStore
import com.sentinela.di.ProximityManager
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.model.RegionRiskCard
import com.sentinela.domain.usecase.GetFiresUseCase
import com.sentinela.domain.usecase.GetRiskUseCase
import com.sentinela.ui.components.LoadableUiState
import com.sentinela.ui.model.PeriodFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapSuccess(
    val fires: List<FireHotspot>,
    val points: List<MonitoredPoint>,
    val regionCards: List<RegionRiskCard>,
    val period: PeriodFilter,
)

class MapViewModel(
    private val getFiresUseCase: GetFiresUseCase,
    private val getRiskUseCase: GetRiskUseCase,
    private val pointRepository: MonitoredPointRepository,
    private val proximityManager: ProximityManager,
    private val firesSessionStore: FiresSessionStore,
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodFilter.H24)
    val period: StateFlow<PeriodFilter> = _period.asStateFlow()

    private val _uiState = MutableStateFlow<LoadableUiState<MapSuccess>>(LoadableUiState.Loading)
    val uiState: StateFlow<LoadableUiState<MapSuccess>> = _uiState.asStateFlow()

    val points: StateFlow<List<MonitoredPoint>> = pointRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            period.collect { loadDashboard(it) }
        }
        viewModelScope.launch {
            points.drop(1).collect { pts ->
                val fires = firesSessionStore.fires
                if (fires.isEmpty()) return@collect
                val evaluated = proximityManager.evaluateAndNotify(pts, fires)
                patchSuccess(evaluated, fires)
            }
        }
    }

    fun setPeriod(filter: PeriodFilter) {
        _period.value = filter
    }

    fun refresh() {
        viewModelScope.launch { loadDashboard(_period.value) }
    }

    private suspend fun loadDashboard(period: PeriodFilter) {
        _uiState.value = LoadableUiState.Loading
        try {
            val fires = getFiresUseCase(period.days)
            firesSessionStore.fires = fires
            val evaluated = proximityManager.evaluateAndNotify(points.value, fires)
            val regionCards = loadRegionalRisks(period.days)
            _uiState.value = LoadableUiState.Success(
                MapSuccess(
                    fires = fires,
                    points = evaluated,
                    regionCards = regionCards,
                    period = period,
                ),
            )
        } catch (e: Exception) {
            _uiState.value = LoadableUiState.Error(
                e.message ?: "Falha ao carregar dados",
            )
        }
    }

    private suspend fun loadRegionalRisks(days: Int): List<RegionRiskCard> = coroutineScope {
        MonitorRegions.ALL.map { region ->
            async {
                runCatching { getRiskUseCase(region.bbox, days) }
                    .fold(
                        onSuccess = { risk ->
                            RegionRiskCard(
                                regionId = region.id,
                                regionName = region.name,
                                risk = risk,
                            )
                        },
                        onFailure = { error ->
                            RegionRiskCard(
                                regionId = region.id,
                                regionName = region.name,
                                error = error.message ?: "Erro ao carregar",
                            )
                        },
                    )
            }
        }.awaitAll()
    }

    private fun patchSuccess(evaluatedPoints: List<MonitoredPoint>, fires: List<FireHotspot>) {
        val current = _uiState.value
        if (current is LoadableUiState.Success) {
            _uiState.value = LoadableUiState.Success(
                current.data.copy(points = evaluatedPoints, fires = fires),
            )
        }
    }
}
