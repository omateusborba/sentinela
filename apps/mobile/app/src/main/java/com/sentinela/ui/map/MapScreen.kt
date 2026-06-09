package com.sentinela.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.ui.components.DashboardHeader
import com.sentinela.ui.components.ErrorContent
import com.sentinela.ui.components.FireMapView
import com.sentinela.ui.components.FireRecentSection
import com.sentinela.ui.components.LoadingContent
import com.sentinela.ui.components.LoadableUiState
import com.sentinela.ui.components.MapLegend
import com.sentinela.ui.components.NearMeCard
import com.sentinela.ui.components.RiskCardsSection
import com.sentinela.ui.components.SectionTitle
import com.sentinela.ui.theme.SentinelaColors

@Composable
fun MapScreen(
    onOpenPoints: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenReport: () -> Unit,
    onFireClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: MapViewModel = viewModel { app.container.mapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val period by viewModel.period.collectAsState()
    val nearMe by viewModel.nearMe.collectAsState()
    val centerOnUser by viewModel.centerOnUser.collectAsState()
    val context = LocalContext.current

    val fireCount = (uiState as? LoadableUiState.Success)?.data?.fires?.size
    val alertCount = (uiState as? LoadableUiState.Success)?.data?.points?.count { it.inAlert } ?: 0
    val loading = uiState is LoadableUiState.Loading

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.refreshNearMe(ok)
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    fun requestNearMe() {
        if (hasLocationPermission()) {
            viewModel.refreshNearMe(true)
        } else {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        requestNearMe()
    }

    Scaffold(
        containerColor = SentinelaColors.Background,
        topBar = {
            DashboardHeader(
                period = period,
                onPeriodChange = viewModel::setPeriod,
                fireCount = fireCount,
                loading = loading,
                onRefresh = { viewModel.refresh() },
                onOpenAlerts = onOpenAlerts,
                onOpenPoints = onOpenPoints,
                periodEnabled = !loading,
                alertCount = alertCount,
            )
        },
        bottomBar = {
            Text(
                text = "Dados: NASA FIRMS · Mapa: OpenStreetMap · API Sentinela · Reportes colaborativos (MVP anônimo)",
                style = MaterialTheme.typography.labelSmall,
                color = SentinelaColors.TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is LoadableUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                DashboardErrorBanner(
                    message = state.message,
                    onRetry = { viewModel.refresh() },
                )
            }
            else -> DashboardContent(
                state = state,
                period = period,
                nearMe = nearMe,
                centerOnUser = centerOnUser,
                onCenterOnUserHandled = viewModel::onCenterOnUserHandled,
                onCenterMap = viewModel::centerMapOnUser,
                onOpenReport = onOpenReport,
                onFireClick = onFireClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: LoadableUiState<MapSuccess>,
    period: com.sentinela.ui.model.PeriodFilter,
    nearMe: NearMeUiState,
    centerOnUser: Boolean,
    onCenterOnUserHandled: () -> Unit,
    onCenterMap: () -> Unit,
    onOpenReport: () -> Unit,
    onFireClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val mapHeight = (screenHeight * 0.38f).coerceIn(220.dp, 420.dp)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (state is LoadableUiState.Success && state.data.points.any { it.inAlert }) {
            item {
                val count = state.data.points.count { it.inAlert }
                Text(
                    text = "$count ponto(s) em alerta",
                    color = SentinelaColors.FlameRed,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        item {
            when (state) {
                is LoadableUiState.Loading -> {
                    BoxWithConstraints {
                        val cols = riskGridColumns(maxWidth)
                        RiskCardsSection(
                            cards = placeholderRiskCards(),
                            gridColumns = cols,
                        )
                    }
                    LoadingContent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 8.dp),
                    )
                }
                is LoadableUiState.Success -> {
                    BoxWithConstraints {
                        RiskCardsSection(
                            cards = state.data.regionCards,
                            gridColumns = riskGridColumns(maxWidth),
                        )
                    }
                }
                else -> Unit
            }
        }

        item {
            NearMeCard(
                state = nearMe,
                onCenterMap = onCenterMap,
            )
        }

        item {
            Button(
                onClick = onOpenReport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SentinelaColors.FlameOrange),
            ) {
                Text("Reportar incêndio")
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wide = maxWidth >= 840.dp
                if (wide) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        MapBlock(
                            days = period.days,
                            mapHeight = mapHeight,
                            loading = state is LoadableUiState.Loading,
                            userLatitude = nearMe.coordinate?.latitude,
                            userLongitude = nearMe.coordinate?.longitude,
                            centerOnUser = centerOnUser,
                            onCenterOnUserHandled = onCenterOnUserHandled,
                            modifier = Modifier.weight(1.15f),
                        )
                        if (state is LoadableUiState.Success) {
                            FireRecentSection(
                                fires = state.data.fires,
                                maxRows = 50,
                                onFireClick = onFireClick,
                                modifier = Modifier.weight(1f),
                            )
                        } else if (state is LoadableUiState.Loading) {
                            LoadingContent(Modifier.weight(1f).height(mapHeight))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        MapBlock(
                            days = period.days,
                            mapHeight = mapHeight,
                            loading = state is LoadableUiState.Loading,
                            userLatitude = nearMe.coordinate?.latitude,
                            userLongitude = nearMe.coordinate?.longitude,
                            centerOnUser = centerOnUser,
                            onCenterOnUserHandled = onCenterOnUserHandled,
                        )
                        when (state) {
                            is LoadableUiState.Success -> FireRecentSection(
                                fires = state.data.fires,
                                maxRows = 50,
                                onFireClick = onFireClick,
                            )
                            is LoadableUiState.Loading -> LoadingContent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                            )
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapBlock(
    days: Int,
    mapHeight: androidx.compose.ui.unit.Dp,
    loading: Boolean,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    centerOnUser: Boolean = false,
    onCenterOnUserHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionTitle(
            text = "Mapa de focos",
            modifier = Modifier.padding(bottom = 8.dp),
        )
        MapLegend(modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        ) {
            if (loading) {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeight),
                )
            } else {
                FireMapView(
                    days = days,
                    userLatitude = userLatitude,
                    userLongitude = userLongitude,
                    centerOnUser = centerOnUser,
                    onCenterOnUserHandled = onCenterOnUserHandled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeight),
                )
            }
        }
    }
}

@Composable
private fun DashboardErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = SentinelaColors.FlameRed.copy(alpha = 0.1f),
        ),
    ) {
        ErrorContent(
            message = message,
            onRetry = onRetry,
            modifier = Modifier.padding(12.dp),
        )
    }
}

private fun riskGridColumns(maxWidth: androidx.compose.ui.unit.Dp): Int = when {
    maxWidth >= 1024.dp -> 5
    maxWidth >= 720.dp -> 3
    maxWidth >= 400.dp -> 2
    else -> 1
}

private fun placeholderRiskCards() = com.sentinela.data.MonitorRegions.ALL.map {
    com.sentinela.domain.model.RegionRiskCard(
        regionId = it.id,
        regionName = it.name,
    )
}
