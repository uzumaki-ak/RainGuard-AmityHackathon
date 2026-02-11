package com.rainguard.ai.ui.utils

import kotlin.math.*

object LocationUtils {
    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            else -> "%.1f km".format(distanceKm)
        }
    }

    /**
     * Format distance in meters
     */
    fun formatDistanceMeters(meters: Int): String {
        return when {
            meters < 1000 -> "$meters m"
            else -> "%.1f km".format(meters / 1000.0)
        }
    }
}