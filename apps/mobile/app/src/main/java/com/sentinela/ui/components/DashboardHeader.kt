package com.sentinela.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sentinela.R
import com.sentinela.ui.model.PeriodFilter
import com.sentinela.ui.theme.SentinelaColors

@Composable
fun DashboardHeader(
    period: PeriodFilter,
    onPeriodChange: (PeriodFilter) -> Unit,
    fireCount: Int?,
    loading: Boolean,
    onRefresh: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenPoints: () -> Unit,
    periodEnabled: Boolean,
    alertCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SentinelaColors.Navy)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.sentinela_lockup),
                contentDescription = "Sentinela",
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f, fill = false),
                contentScale = ContentScale.Fit,
            )
            Row {
                if (alertCount > 0) {
                    IconButton(onClick = onOpenAlerts) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Alertas",
                            tint = SentinelaColors.FlameOrange,
                        )
                    }
                } else {
                    IconButton(onClick = onOpenAlerts) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Alertas",
                            tint = Color.White,
                        )
                    }
                }
                IconButton(onClick = onOpenPoints) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Pontos monitorados",
                        tint = Color.White,
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar",
                        tint = Color.White,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PeriodFilterSegmented(
                selected = period,
                onSelected = onPeriodChange,
                modifier = Modifier.weight(1f),
                enabled = periodEnabled,
            )
            Text(
                text = when {
                    loading -> "Carregando…"
                    fireCount != null -> "$fireCount focos"
                    else -> "—"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}
