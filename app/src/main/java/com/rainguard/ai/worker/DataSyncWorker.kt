package com.rainguard.ai.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rainguard.ai.data.repository.RiskZoneRepository
import com.rainguard.ai.data.repository.ShelterRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val riskZoneRepository: RiskZoneRepository,
    private val shelterRepository: ShelterRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting data sync")
            riskZoneRepository.refreshZones()
            shelterRepository.refreshShelters()
            Timber.d("Data sync completed")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error syncing data")
            Result.retry()
        }
    }
}