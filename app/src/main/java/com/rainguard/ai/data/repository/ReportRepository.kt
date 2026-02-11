package com.rainguard.ai.data.repository

import com.rainguard.ai.data.local.database.dao.ReportDao
import com.rainguard.ai.data.local.database.entities.ReportEntity
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    fun getReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toReport() }
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
        return try {
            val report = ReportEntity(
                id = "r_${System.currentTimeMillis()}",
                type = type.name.lowercase(),
                lat = lat,
                lng = lng,
                photoUrl = photoUrl,
                description = description,
                timestamp = java.time.Instant.now().toString(),
                verified = false, // Citizen reports start unverified
                reporterContact = contact,
                uploaded = false
            )
            reportDao.insert(report)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error submitting report")
            Result.Error(e)
        }
    }

    suspend fun verifyReport(reportId: String) {
        try {
            reportDao.getReportById(reportId)?.let { entity ->
                reportDao.update(entity.copy(verified = true))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error verifying report")
        }
    }

    suspend fun deleteReport(reportId: String) {
        try {
            reportDao.deleteById(reportId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting report")
        }
    }

    suspend fun getUnuploadedReports(): List<Report> {
        return reportDao.getUnuploadedReports().map { it.toReport() }
    }
}