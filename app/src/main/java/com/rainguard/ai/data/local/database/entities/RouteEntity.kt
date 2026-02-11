package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val shelterId: String,
    val shelterName: String,
    val pathJson: String,
    val etaMinutes: Int,
    val distanceMeters: Int,
    val confidence: Float,
    val segmentsJson: String,
    val sourcesJson: String,
    val rationaleJson: String
)