package com.sentinela.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinela.domain.model.RegionRisk
import com.sentinela.domain.model.RegionRiskCard
import com.sentinela.domain.model.RiskLevel
import com.sentinela.ui.theme.SentinelaColors
import com.sentinela.ui.theme.riskAccentColor
import com.sentinela.ui.theme.riskCardColors
import com.sentinela.ui.theme.riskLevelLabel
import com.sentinela.ui.theme.riskRegionNameColor
import com.sentinela.ui.theme.riskTrendLabel

@Composable
fun RiskCardsSection(
    cards: List<RegionRiskCard>,
    gridColumns: Int,
    modifier: Modifier = Modifier,
) {
    val columns = gridColumns.coerceAtLeast(1)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle(text = "Risco por região")
        cards.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { card ->
                    RegionRiskCardItem(
                        card = card,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RegionRiskCardItem(
    card: RegionRiskCard,
    modifier: Modifier = Modifier,
) {
    val level = card.risk?.level
    val isHigh = level == RiskLevel.HIGH
    val shape = RoundedCornerShape(10.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = if (level != null) {
            riskCardColors(level)
        } else {
            CardDefaults.cardColors(
                containerColor = SentinelaColors.Surface,
                contentColor = SentinelaColors.Text,
            )
        },
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
        ) {
            if (level != null && !isHigh) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(riskAccentColor(level)),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
            ) {
                Text(
                    text = card.regionName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = riskRegionNameColor(level),
                )
                when {
                    card.error != null -> Text(
                        text = card.error,
                        style = MaterialTheme.typography.labelSmall,
                        color = SentinelaColors.FlameRed,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    card.risk != null -> RiskCardBody(card.risk)
                    else -> Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SentinelaColors.TextMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskCardBody(risk: RegionRisk) {
    val isHigh = risk.level == RiskLevel.HIGH
    val contentColor = if (isHigh) Color.White else SentinelaColors.Text
    val mutedColor = if (isHigh) Color.White.copy(alpha = 0.85f) else SentinelaColors.TextMuted
    Text(
        text = riskLevelLabel(risk.level).uppercase(),
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        ),
        color = contentColor,
        modifier = Modifier.padding(top = 4.dp),
    )
    Text(
        text = "Score ${risk.score.toInt()} / 100",
        style = MaterialTheme.typography.bodyMedium,
        color = contentColor,
        modifier = Modifier.padding(top = 2.dp),
    )
    Text(
        text = "${risk.totalFires} focos · ${riskTrendLabel(risk.trend)}",
        style = MaterialTheme.typography.labelSmall,
        color = mutedColor,
        modifier = Modifier.padding(top = 4.dp),
    )
}
