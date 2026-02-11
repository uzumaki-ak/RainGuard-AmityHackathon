package com.rainguard.ai.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?,
    @Json(name = "finishReason") val finishReason: String?,
    @Json(name = "safetyRatings") val safetyRatings: List<GeminiSafetyRating>?
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>?,
    @Json(name = "role") val role: String?
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class GeminiSafetyRating(
    @Json(name = "category") val category: String?,
    @Json(name = "probability") val probability: String?
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)