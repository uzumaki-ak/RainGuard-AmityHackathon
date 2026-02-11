package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RiskZoneDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "geojson") val geojson: GeoJsonDto,
    @Json(name = "sources") val sources: List<String>,
    @Json(name = "updated") val updated: String,
    @Json(name = "reason") val reason: String
)

@JsonClass(generateAdapter = true)
data class GeoJsonDto(
    @Json(name = "type") val type: String,
    @Json(name = "geometry") val geometry: GeometryDto
)

@JsonClass(generateAdapter = true)
data class GeometryDto(
    @Json(name = "type") val type: String,
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>>
)