package com.rainguard.ai.data.model

data class Alert(
    val id: String,
    val title: String,
    val message: String,
    val severity: RiskSeverity,
    val confidence: Float,
    val zoneId: String,
    val timestamp: String,
    val expiresAt: String,
    val mapThumbnail: String,
    val recommendedActions: List<String>,
    val acknowledged: Boolean = false
)