package com.rainguard.ai.ui.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

    fun formatTimestamp(timestamp: String): String {
        return try {
            val date = dateFormat.parse(timestamp)
            date?.let { displayFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    fun formatTime(timestamp: String): String {
        return try {
            val date = dateFormat.parse(timestamp)
            date?.let { timeFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    fun getRelativeTime(timestamp: String): String {
        return try {
            val date = dateFormat.parse(timestamp) ?: return timestamp
            val now = Date()
            val diff = now.time - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hr ago"
                days < 7 -> "$days days ago"
                else -> formatTimestamp(timestamp)
            }
        } catch (e: Exception) {
            timestamp
        }
    }
}