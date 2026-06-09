package com.sentinela.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.SentinelaDefaults
import com.sentinela.data.MonitorRegions
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.data.repository.ReportRepository
import com.sentinela.di.FiresSessionStore
import com.sentinela.di.ProximityManager
import com.sentinela.domain.model.Coordinate
import com.sentinela.domain.model.FireHotspot
import com.sentinela.domain.model.FireReport
import com.sentinela.domain.model.MonitoredPoint
import com.sentinela.domain.model.RegionRiskCard
import com.sentinela.domain.usecase.EvaluateProximityUseCase
import com.sentinela.domain.usecase.GetFiresUseCase
import com.sentinela.domain.usecase.GetRiskUseCase
import com.sentinela.domain.util.haversineKm
import com.sentinela.location.LocationProvider
import com.sentinela.notification.NotificationHelper
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
    val reports: List<FireReport>,
    val period: PeriodFilter,
)

data class NearMeUiState(
    val coordinate: Coordinate? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val nearestDistanceKm: Double? = null,
    val firesInRadius: Int = 0,
    val inAlert: Boolean = false,
)

class MapViewModel(
    private val getFiresUseCase: GetFiresUseCase,
    private val getRiskUseCase: GetRiskUseCase,
    private val reportRepository: ReportRepository,
    private val pointRepository: MonitoredPointRepository,
    private val proximityManager: ProximityManager,
    private val evaluateProximityUseCase: EvaluateProximityUseCase,
    private val locationProvider: LocationProvider,
    private val notificationHelper: NotificationHelper,
    private val firesSessionStore: FiresSessionStore,
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodFilter.H24)
    val period: StateFlow<PeriodFilter> = _period.asStateFlow()

    private val _uiState = MutableStateFlow<LoadableUiState<MapSuccess>>(LoadableUiState.Loading)
    val uiState: StateFlow<LoadableUiState<MapSuccess>> = _uiState.asStateFlow()

    private val _nearMe = MutableStateFlow(NearMeUiState(loading = true))
    val nearMe: StateFlow<NearMeUiState> = _nearMe.asStateFlow()

    private val _centerOnUser = MutableStateFlow(false)
    val centerOnUser: StateFlow<Boolean> = _centerOnUser.asStateFlow()

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
                patchSuccess(evaluated, fires, null)
            }
        }
    }

    fun setPeriod(filter: PeriodFilter) {
        _period.value = filter
    }

    fun refresh() {
        viewModelScope.launch { loadDashboard(_period.value) }
    }

    fun refreshNearMe(hasLocationPermission: Boolean) {
        if (!hasLocationPermission) {
            _nearMe.value = _nearMe.value.copy(
                error = "Permissão de localização necessária.",
                loading = false,
            )
            return
        }
        viewModelScope.launch {
            _nearMe.value = _nearMe.value.copy(loading = true, error = null)
            val coordinate = locationProvider.getCurrentLocation()
            if (coordinate == null) {
                _nearMe.value = _nearMe.value.copy(
                    loading = false,
                    error = "Não foi possível obter sua localização.",
                )
                return@launch
            }
            evaluateNearMe(coordinate)
        }
    }

    fun centerMapOnUser() {
        if (_nearMe.value.coordinate != null) {
            _centerOnUser.value = true
        }
    }

    fun onCenterOnUserHandled() {
        _centerOnUser.value = false
    }

    private fun evaluateNearMe(coordinate: Coordinate) {
        val fires = firesSessionStore.fires
        var nearestDistance: Double? = null
        var firesInRadius = 0
        for (fire in fires) {
            val distance = haversineKm(
                coordinate.latitude,
                coordinate.longitude,
                fire.latitude,
                fire.longitude,
            )
            if (nearestDistance == null || distance < nearestDistance) {
                nearestDistance = distance
            }
            if (distance <= SentinelaDefaults.NEAR_ME_RADIUS_KM) {
                firesInRadius++
            }
        }

        val userPoint = MonitoredPoint(
            id = SentinelaDefaults.USER_LOCATION_POINT_ID,
            name = "Minha localização",
            latitude = coordinate.latitude,
            longitude = coordinate.longitude,
            radiusKm = SentinelaDefaults.NEAR_ME_RADIUS_KM,
            inAlert = _nearMe.value.inAlert,
        )
        val proximity = evaluateProximityUseCase(listOf(userPoint), fires)
        val inAlert = proximity.updatedPoints.firstOrNull()?.inAlert ?: false
        proximity.newAlerts.forEach { notificationHelper.showProximityAlert(it) }

        _nearMe.value = NearMeUiState(
            coordinate = coordinate,
            loading = false,
            error = null,
            nearestDistanceKm = nearestDistance,
            firesInRadius = firesInRadius,
            inAlert = inAlert,
        )
    }

    private suspend fun loadDashboard(period: PeriodFilter) {
        _uiState.value = LoadableUiState.Loading
        try {
            val fires = getFiresUseCase(period.days)
            firesSessionStore.fires = fires
            val evaluated = proximityManager.evaluateAndNotify(points.value, fires)
            val regionCards = loadRegionalRisks(period.days)
            val reports = runCatching { reportRepository.getReports() }.getOrDefault(emptyList())
            _uiState.value = LoadableUiState.Success(
                MapSuccess(
                    fires = fires,
                    points = evaluated,
                    regionCards = regionCards,
                    reports = reports,
                    period = period,
                ),
            )
            _nearMe.value.coordinate?.let { evaluateNearMe(it) }
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

    private fun patchSuccess(
        evaluatedPoints: List<MonitoredPoint>,
        fires: List<FireHotspot>,
        reports: List<FireReport>?,
    ) {
        val current = _uiState.value
        if (current is LoadableUiState.Success) {
            _uiState.value = LoadableUiState.Success(
                current.data.copy(
                    points = evaluatedPoints,
                    fires = fires,
                    reports = reports ?: current.data.reports,
                ),
            )
        }
    }
}
