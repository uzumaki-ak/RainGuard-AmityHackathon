package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteDto(
    @Json(name = "id") val id: String,
    @Json(name = "shelterId") val shelterId: String,
    @Json(name = "shelterName") val shelterName: String,
    @Json(name = "path") val path: List<List<Double>>,
    @Json(name = "etaMinutes") val etaMinutes: Int,
    @Json(name = "distanceMeters") val distanceMeters: Int,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "segments") val segments: List<RouteSegmentDto>,
    @Json(name = "sources") val sources: List<String>,
    @Json(name = "rationale") val rationale: List<String>
)

@JsonClass(generateAdapter = true)
data class RouteSegmentDto(
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "distanceMeters") val distanceMeters: Int,
    @Json(name = "etaSeconds") val etaSeconds: Int,
    @Json(name = "hazards") val hazards: List<String>
)

@JsonClass(generateAdapter = true)
data class RouteRequest(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "destinationId") val destinationId: String
)