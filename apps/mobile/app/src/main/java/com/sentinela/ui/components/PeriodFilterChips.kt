package com.sentinela.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sentinela.ui.model.PeriodFilter

@Composable
fun PeriodFilterChips(
    selected: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodFilter.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelected(period) },
                label = { Text(period.label) },
                enabled = enabled,
            )
        }
    }
}
