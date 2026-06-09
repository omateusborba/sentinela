package com.sentinela.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sentinela.SentinelaDefaults
import com.sentinela.ui.map.NearMeUiState
import com.sentinela.ui.theme.SentinelaColors

@Composable
fun NearMeCard(
    state: NearMeUiState,
    onCenterMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionTitle(
            text = "Perto de mim",
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.inAlert) {
                    SentinelaColors.FlameRed.copy(alpha = 0.08f)
                } else {
                    SentinelaColors.Surface
                },
            ),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (state.loading && state.coordinate == null) {
                    Text(
                        text = "Obtendo localização…",
                        style = MaterialTheme.typography.bodySmall,
                        color = SentinelaColors.TextMuted,
                    )
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = SentinelaColors.FlameRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = if (state.loading) 8.dp else 0.dp),
                    )
                }

                state.coordinate?.let { coord ->
                    Text(
                        text = "Você: ${coord.latitude.format(4)}, ${coord.longitude.format(4)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SentinelaColors.TextMuted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    when {
                        state.nearestDistanceKm != null -> {
                            Text(
                                text = "Foco mais próximo: ${state.nearestDistanceKm.format(1)} km",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SentinelaColors.Text,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            if (state.inAlert) {
                                Text(
                                    text = "⚠ ${state.firesInRadius} foco(s) dentro de ${SentinelaDefaults.NEAR_ME_RADIUS_KM.toInt()} km",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SentinelaColors.FlameRed,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            } else {
                                Text(
                                    text = "Nenhum foco de satélite dentro de ${SentinelaDefaults.NEAR_ME_RADIUS_KM.toInt()} km.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SentinelaColors.RiskLow,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                        else -> Text(
                            text = "Nenhum foco no período selecionado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SentinelaColors.TextMuted,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    OutlinedButton(
                        onClick = onCenterMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text("Centralizar em mim", color = SentinelaColors.Navy)
                    }
                }
            }
        }
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
