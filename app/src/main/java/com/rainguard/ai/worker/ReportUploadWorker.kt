package com.rainguard.ai.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rainguard.ai.data.repository.ReportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ReportUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reportRepository: ReportRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val unuploadedReports = reportRepository.getUnuploadedReports()
            Timber.d("Found ${unuploadedReports.size} reports to upload")

            // In production, upload each report
            unuploadedReports.forEach { report ->
                // Mock upload - in production call API
                Timber.d("Uploaded report ${report.id}")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading reports")
            Result.retry()
        }
    }
}