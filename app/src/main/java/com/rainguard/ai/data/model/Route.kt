package com.rainguard.ai.data.model

data class Route(
    val id: String,
    val shelterId: String,
    val shelterName: String,
    val path: List<List<Double>>, // [lng, lat] coordinates
    val etaMinutes: Int,
    val distanceMeters: Int,
    val confidence: Float,
    val segments: List<RouteSegment>,
    val sources: List<String>,
    val rationale: List<String>
)

data class RouteSegment(
    val from: String,
    val to: String,
    val distanceMeters: Int,
    val etaSeconds: Int,
    val hazards: List<String>
)