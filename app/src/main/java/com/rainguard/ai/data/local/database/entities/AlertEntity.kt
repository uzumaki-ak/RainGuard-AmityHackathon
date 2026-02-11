package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rainguard.ai.data.model.Alert
import com.rainguard.ai.data.model.RiskSeverity

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val confidence: Float,
    val zoneId: String,
    val timestamp: String,
    val expiresAt: String,
    val mapThumbnail: String,
    val actionsJson: String,
    val acknowledged: Boolean = false
) {
    fun toAlert(actions: List<String>): Alert {
        return Alert(
            id = id,
            title = title,
            message = message,
            severity = RiskSeverity.fromString(severity),
            confidence = confidence,
            zoneId = zoneId,
            timestamp = timestamp,
            expiresAt = expiresAt,
            mapThumbnail = mapThumbnail,
            recommendedActions = actions,
            acknowledged = acknowledged
        )
    }
}