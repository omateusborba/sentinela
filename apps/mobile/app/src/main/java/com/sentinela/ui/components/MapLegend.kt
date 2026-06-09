package com.sentinela.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sentinela.ui.theme.SentinelaColors

private val ReportBlue = Color(0xFF2563EB)
private val ReportPurple = Color(0xFF7C3AED)
private val ReportViolet = Color(0xFF9333EA)

@Composable
fun MapLegend(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Satélite:", style = MaterialTheme.typography.labelSmall, color = SentinelaColors.TextMuted)
            LegendDot("Baixa", SentinelaColors.ConfidenceLow)
            LegendDot("Nominal", SentinelaColors.ConfidenceNominal)
            LegendDot("Alta", SentinelaColors.ConfidenceHigh)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Comunidade:", style = MaterialTheme.typography.labelSmall, color = SentinelaColors.TextMuted)
            LegendDot("Baixa", ReportBlue)
            LegendDot("Média", ReportPurple)
            LegendDot("Alta", ReportViolet)
            LegendDot("Você", ReportBlue)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = SentinelaColors.TextMuted)
    }
}
