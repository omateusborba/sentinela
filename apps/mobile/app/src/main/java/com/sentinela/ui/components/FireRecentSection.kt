package com.sentinela.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinela.domain.model.FireHotspot
import com.sentinela.ui.theme.SentinelaColors
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val confidencePt = mapOf(
    "low" to "Baixa",
    "nominal" to "Nominal",
    "high" to "Alta",
)

private val borderColor = Color(0xFFD8DCE6)

@Composable
fun FireRecentSection(
    fires: List<FireHotspot>,
    maxRows: Int,
    onFireClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = remember(fires, maxRows) {
        fires
            .sortedByDescending { runCatching { Instant.parse(it.acquiredAt) }.getOrElse { Instant.EPOCH } }
            .take(maxRows)
    }

    Column(modifier = modifier) {
        SectionTitle(
            text = "Focos recentes",
            modifier = Modifier.padding(bottom = 10.dp),
        )
        if (rows.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SentinelaColors.Surface),
            ) {
                Text(
                    text = "Nenhum foco no período selecionado.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SentinelaColors.TextMuted,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { fire ->
                    FireRecentCard(
                        fire = fire,
                        onClick = { onFireClick(fire.id) },
                    )
                }
            }
            if (fires.size > maxRows) {
                Text(
                    text = "Exibindo $maxRows de ${fires.size} focos (mais recentes primeiro).",
                    style = MaterialTheme.typography.labelSmall,
                    color = SentinelaColors.TextMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun FireRecentCard(
    fire: FireHotspot,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = SentinelaColors.Surface,
            contentColor = SentinelaColors.Text,
        ),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDateTime(fire.acquiredAt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SentinelaColors.Text,
                )
                ConfidenceBadge(fire.confidence)
            }
            FireDetailRow("Coordenadas", "${fire.latitude.format(4)}, ${fire.longitude.format(4)}")
            val sensor = buildString {
                append(fire.satellite)
                if (fire.instrument.isNotBlank()) append(" / ${fire.instrument}")
            }
            FireDetailRow("Sensor", sensor)
            FireDetailRow(
                "FRP",
                fire.frp?.let { "${it.format(1)} MW" } ?: "—",
            )
        }
    }
}

@Composable
private fun FireDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = SentinelaColors.TextMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SentinelaColors.Text,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ConfidenceBadge(confidence: String) {
    val label = confidencePt[confidence.lowercase()] ?: confidence
    val (bg, fg) = when (confidence.lowercase()) {
        "low" -> Color(0x261E9E5B) to SentinelaColors.RiskLow
        "high" -> Color(0x26E8401E) to SentinelaColors.FlameRed
        else -> Color(0x26F4731E) to SentinelaColors.FlameOrange
    }
    Text(
        text = label,
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = fg,
    )
}

private val timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy, HH:mm")
    .withZone(ZoneOffset.UTC)

private fun formatDateTime(iso: String): String =
    runCatching { timeFormatter.format(Instant.parse(iso)) }.getOrDefault(iso)

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
