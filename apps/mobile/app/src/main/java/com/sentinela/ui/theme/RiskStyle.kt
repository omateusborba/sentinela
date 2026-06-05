package com.sentinela.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sentinela.domain.model.RiskLevel

@Composable
fun riskCardColors(level: RiskLevel): CardColors {
    return when (level) {
        RiskLevel.LOW -> CardDefaults.cardColors(
            containerColor = SentinelaColors.Surface,
            contentColor = SentinelaColors.Text,
        )
        RiskLevel.MEDIUM -> CardDefaults.cardColors(
            containerColor = SentinelaColors.Surface,
            contentColor = SentinelaColors.Text,
        )
        RiskLevel.HIGH -> CardDefaults.cardColors(
            containerColor = SentinelaColors.RiskHigh,
            contentColor = Color.White,
        )
    }
}

fun riskAccentColor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> SentinelaColors.RiskLow
    RiskLevel.MEDIUM -> SentinelaColors.RiskMedium
    RiskLevel.HIGH -> SentinelaColors.RiskHigh
}
