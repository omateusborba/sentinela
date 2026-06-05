package com.sentinela.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sentinela.ui.theme.SentinelaColors

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        color = SentinelaColors.TextMuted,
    )
}
