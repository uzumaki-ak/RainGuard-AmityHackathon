package com.rainguard.ai.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rainguard.ai.data.local.database.dao.AlertDao
import com.rainguard.ai.data.model.Alert
import com.rainguard.ai.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao,
    private val gson: Gson
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val safeSignals = firestore.collection("safe_signals")

    fun getAllAlerts(): Flow<List<Alert>> {
        return alertDao.getAllAlerts().map { entities ->
            entities.map { entity ->
                val actions = gson.fromJson<List<String>>(
                    entity.actionsJson,
                    object : TypeToken<List<String>>() {}.type
                )
                entity.toAlert(actions)
            }
        }
    }

    fun getActiveAlerts(): Flow<List<Alert>> {
        return alertDao.getActiveAlerts().map { entities ->
            entities.map { entity ->
                val actions = gson.fromJson<List<String>>(
                    entity.actionsJson,
                    object : TypeToken<List<String>>() {}.type
                )
                entity.toAlert(actions)
            }
        }
    }

    suspend fun acknowledgeAlert(alertId: String): Result<Unit> {
        return try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error acknowledging alert")
            Result.Error(e)
        }
    }

    suspend fun sendSafeStatus(alertId: String, location: Pair<Double, Double>?, message: String?): Result<Unit> {
        return try {
            // LIVE SYNC TO AUTHORITY
            val signal = hashMapOf(
                "alertId" to alertId,
                "timestamp" to java.time.Instant.now().toString(),
                "status" to "SAFE",
                "userName" to "Citizen User", // Mock name
                "location" to location?.let { "${it.first}, ${it.second}" }
            )
            safeSignals.add(signal).await()
            
            Timber.d("Safe status sent LIVE for alert $alertId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error sending safe status")
            Result.Error(e)
        }
    }
}
