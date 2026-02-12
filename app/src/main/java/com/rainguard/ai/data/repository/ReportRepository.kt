package com.rainguard.ai.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.rainguard.ai.data.local.database.dao.ReportDao
import com.rainguard.ai.data.local.database.entities.ReportEntity
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection("reports")

    fun getReports(): Flow<List<Report>> {
        // COMBINE ROOM + FIRESTORE LIVE
        return combine(
            reportDao.getAllReports().map { entities -> entities.map { it.toReport() } },
            reportsCollection.snapshots().map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        Report(
                            id = doc.id,
                            type = ReportType.valueOf((doc.getString("type") ?: "OTHER").uppercase()),
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0,
                            photoUrl = doc.getString("photoUrl"),
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getString("timestamp") ?: "",
                            verified = doc.getBoolean("verified") ?: false,
                            reporterContact = doc.getString("reporterContact")
                        )
                    } catch (e: Exception) { null }
                }
            }
        ) { local, remote ->
            (local + remote).distinctBy { it.id }
        }
    }

    suspend fun submitReport(
        type: ReportType,
        lat: Double,
        lng: Double,
        photoUrl: String?,
        description: String,
        contact: String?
    ): Result<Unit> {
        val reportId = "r_${System.currentTimeMillis()}"
        val timestamp = java.time.Instant.now().toString()
        
        val reportData = hashMapOf(
            "type" to type.name.lowercase(),
            "lat" to lat,
            "lng" to lng,
            "photoUrl" to photoUrl,
            "description" to description,
            "timestamp" to timestamp,
            "verified" to false,
            "reporterContact" to contact
        )

        return try {
            // 1. SAVE TO FIRESTORE (LIVE FOR OTHERS)
            reportsCollection.document(reportId).set(reportData).await()
            
            // 2. SAVE TO ROOM (BACKUP)
            val entity = ReportEntity(
                id = reportId,
                type = type.name.lowercase(),
                lat = lat,
                lng = lng,
                photoUrl = photoUrl,
                description = description,
                timestamp = timestamp,
                verified = false,
                reporterContact = contact,
                uploaded = true
            )
            reportDao.insert(entity)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error submitting live report")
            Result.Error(e)
        }
    }

    suspend fun verifyReport(reportId: String) {
        try {
            reportsCollection.document(reportId).update("verified", true).await()
            reportDao.getReportById(reportId)?.let { entity ->
                reportDao.update(entity.copy(verified = true))
            }
        } catch (e: Exception) { Timber.e(e, "Error verifying") }
    }

    suspend fun deleteReport(reportId: String) {
        try {
            reportsCollection.document(reportId).delete().await()
            reportDao.deleteById(reportId)
        } catch (e: Exception) { Timber.e(e, "Error deleting") }
    }

    suspend fun getUnuploadedReports(): List<Report> {
        return reportDao.getUnuploadedReports().map { it.toReport() }
    }
}
