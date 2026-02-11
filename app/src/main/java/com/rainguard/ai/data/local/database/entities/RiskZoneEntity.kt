package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rainguard.ai.data.model.RiskSeverity
import com.rainguard.ai.data.model.RiskZone

@Entity(tableName = "risk_zones")
data class RiskZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val severity: String,
    val confidence: Float,
    val coordinatesJson: String, // Stored as JSON string
    val sourcesJson: String,
    val updated: String,
    val reason: String
) {
    fun toRiskZone(coordinates: List<List<Double>>, sources: List<String>): RiskZone {
        return RiskZone(
            id = id,
            name = name,
            severity = RiskSeverity.fromString(severity),
            confidence = confidence,
            coordinates = coordinates,
            sources = sources,
            updated = updated,
            reason = reason
        )
    }
}