package com.rainguard.ai.data.model

data class RiskZone(
    val id: String,
    val name: String,
    val severity: RiskSeverity,
    val confidence: Float,
    val coordinates: List<List<Double>>, // [lng, lat] pairs for polygon
    val sources: List<String>,
    val updated: String,
    val reason: String
)

enum class RiskSeverity {
    HIGH, MEDIUM, LOW;

    companion object {
        fun fromString(value: String): RiskSeverity {
            return when (value.lowercase()) {
                "high" -> HIGH
                "medium" -> MEDIUM
                "low" -> LOW
                else -> LOW
            }
        }
    }
}