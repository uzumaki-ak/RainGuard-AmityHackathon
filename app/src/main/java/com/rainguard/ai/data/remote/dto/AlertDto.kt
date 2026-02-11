package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "zoneId") val zoneId: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "expiresAt") val expiresAt: String,
    @Json(name = "mapThumbnail") val mapThumbnail: String,
    @Json(name = "recommendedActions") val recommendedActions: List<String>
)