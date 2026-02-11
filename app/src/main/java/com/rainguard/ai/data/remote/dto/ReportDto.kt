package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "photoUrl") val photoUrl: String?,
    @Json(name = "description") val description: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "verified") val verified: Boolean,
    @Json(name = "reporterContact") val reporterContact: String?
)

@JsonClass(generateAdapter = true)
data class CreateReportRequest(
    @Json(name = "type") val type: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "description") val description: String,
    @Json(name = "contact") val contact: String?
)