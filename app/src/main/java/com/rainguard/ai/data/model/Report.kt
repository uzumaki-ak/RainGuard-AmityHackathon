package com.rainguard.ai.data.model

data class Report(
    val id: String,
    val type: ReportType,
    val lat: Double,
    val lng: Double,
    val photoUrl: String?,
    val description: String,
    val timestamp: String,
    val verified: Boolean,
    val reporterContact: String?
)

enum class ReportType {
    FLOOD, EROSION, BLOCKED_ROAD, REQUEST_HELP, OTHER;

    companion object {
        fun fromString(value: String): ReportType {
            return when (value.lowercase()) {
                "flood" -> FLOOD
                "erosion" -> EROSION
                "blocked_road" -> BLOCKED_ROAD
                "request_help" -> REQUEST_HELP
                else -> OTHER
            }
        }
    }

    fun toDisplayString(): String {
        return when (this) {
            FLOOD -> "Flood"
            EROSION -> "Erosion"
            BLOCKED_ROAD -> "Blocked Road"
            REQUEST_HELP -> "Request Help"
            OTHER -> "Other"
        }
    }
}