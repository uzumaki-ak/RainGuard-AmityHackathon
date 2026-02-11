package com.rainguard.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rainguard.ai.data.model.RiskSeverity
import com.rainguard.ai.ui.theme.RiskHigh
import com.rainguard.ai.ui.theme.RiskLow
import com.rainguard.ai.ui.theme.RiskMedium

@Composable
fun RiskLevelBadge(
    severity: RiskSeverity,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (severity) {
        RiskSeverity.HIGH -> RiskHigh to "HIGH"
        RiskSeverity.MEDIUM -> RiskMedium to "MEDIUM"
        RiskSeverity.LOW -> RiskLow to "LOW"
    }

    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}