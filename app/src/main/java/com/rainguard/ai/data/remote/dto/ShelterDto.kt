package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShelterDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "capacity") val capacity: Int,
    @Json(name = "available") val available: Int,
    @Json(name = "address") val address: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "open") val open: Boolean,
    @Json(name = "wheelchairAccessible") val wheelchairAccessible: Boolean,
    @Json(name = "supplies") val supplies: SuppliesDto
)

@JsonClass(generateAdapter = true)
data class SuppliesDto(
    @Json(name = "food") val food: Boolean,
    @Json(name = "water") val water: Boolean,
    @Json(name = "medical") val medical: Boolean,
    @Json(name = "blankets") val blankets: Boolean
)