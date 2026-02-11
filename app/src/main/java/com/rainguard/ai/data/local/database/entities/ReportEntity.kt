package com.rainguard.ai.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.ReportType

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val type: String,
    val lat: Double,
    val lng: Double,
    val photoUrl: String?,
    val description: String,
    val timestamp: String,
    val verified: Boolean,
    val reporterContact: String?,
    val uploaded: Boolean = false
) {
    fun toReport(): Report {
        return Report(
            id = id,
            type = ReportType.fromString(type),
            lat = lat,
            lng = lng,
            photoUrl = photoUrl,
            description = description,
            timestamp = timestamp,
            verified = verified,
            reporterContact = reporterContact
        )
    }
}