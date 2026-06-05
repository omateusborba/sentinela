package com.sentinela.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentinela.SentinelaApplication
import com.sentinela.domain.model.RiskLevel
import com.sentinela.ui.components.ErrorContent
import com.sentinela.ui.components.LoadingContent
import com.sentinela.ui.components.LoadableUiState
import com.sentinela.ui.components.FireMapView
import com.sentinela.ui.components.PeriodFilterChips
import com.sentinela.ui.components.SentinelaTopAppBar
import com.sentinela.ui.theme.SentinelaColors
import com.sentinela.ui.theme.riskAccentColor
import com.sentinela.ui.theme.riskCardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onOpenFires: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenAlerts: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SentinelaApplication
    val viewModel: MapViewModel = viewModel { app.container.mapViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val period by viewModel.period.collectAsState()
    val alertCount = (uiState as? LoadableUiState.Success)?.data?.points?.count { it.inAlert } ?: 0

    Scaffold(
        topBar = {
            SentinelaTopAppBar(
                title = "Sentinela",
                showLogo = true,
                actions = {
                    IconButton(onClick = onOpenAlerts) {
                        Icon(Icons.Default.Warning, contentDescription = "Alertas")
                    }
                    IconButton(onClick = onOpenFires) {
                        Icon(Icons.Default.List, contentDescription = "Lista de focos")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenPoints,
                containerColor = SentinelaColors.FlameOrange,
                contentColor = androidx.compose.ui.graphics.Color.White,
                elevation = FloatingActionButtonDefaults.elevation(),
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Pontos monitorados")
            }
        },
    ) { padding ->
        val mapHeight = (LocalConfiguration.current.screenHeightDp * 0.5f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PeriodFilterChips(
                selected = period,
                onSelected = viewModel::setPeriod,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = uiState !is LoadableUiState.Loading,
            )

            when (val state = uiState) {
                is LoadableUiState.Loading -> LoadingContent(
                    Modifier.fillMaxWidth().height(mapHeight),
                )
                is LoadableUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.fillMaxWidth().height(mapHeight),
                )
                is LoadableUiState.Success -> {
                    state.data.risk?.let { risk ->
                        RiskBanner(risk.level, risk.score, risk.totalFires)
                    }
                    if (alertCount > 0) {
                        Text(
                            text = "$alertCount ponto(s) em alerta",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = SentinelaColors.FlameRed,
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        FireMapView(
                            days = period.days,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(mapHeight),
                        )
                    }
                    Text(
                        text = "© OpenStreetMap contributors",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskBanner(level: RiskLevel, score: Double, totalFires: Int) {
    val label = when (level) {
        RiskLevel.LOW -> "Risco baixo"
        RiskLevel.MEDIUM -> "Risco médio"
        RiskLevel.HIGH -> "Risco alto"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = riskCardColors(level),
    ) {
        Text(
            text = "$label · score ${score.toInt()} · $totalFires focos (Brasil)",
            modifier = Modifier.padding(12.dp),
            color = if (level == RiskLevel.HIGH) {
                androidx.compose.ui.graphics.Color.White
            } else {
                riskAccentColor(level)
            },
        )
    }
}
