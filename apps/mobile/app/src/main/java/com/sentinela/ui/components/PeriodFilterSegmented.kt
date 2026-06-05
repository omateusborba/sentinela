package com.sentinela.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sentinela.ui.model.PeriodFilter
import com.sentinela.ui.theme.SentinelaColors

@Composable
fun PeriodFilterSegmented(
    selected: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SentinelaColors.NavyDeep)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PeriodFilter.entries.forEach { period ->
            val active = selected == period
            Text(
                text = period.label,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (active) SentinelaColors.FlameOrange else Color.Transparent,
                    )
                    .clickable(enabled = enabled) { onSelected(period) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .semantics { role = Role.Button },
                style = MaterialTheme.typography.labelMedium,
                color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
