package com.rainguard.ai.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rainguard.ai.data.local.database.dao.RiskZoneDao
import com.rainguard.ai.data.local.database.entities.RiskZoneEntity
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.model.RiskSeverity
import com.rainguard.ai.data.model.RiskZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskZoneRepository @Inject constructor(
    private val riskZoneDao: RiskZoneDao,
    private val gson: Gson
) {
    fun getRiskZones(): Flow<List<RiskZone>> {
        return riskZoneDao.getAllZones().map { entities ->
            entities.map { entity ->
                val coordinates = gson.fromJson<List<List<Double>>>(
                    entity.coordinatesJson,
                    object : TypeToken<List<List<Double>>>() {}.type
                )
                val sources = gson.fromJson<List<String>>(
                    entity.sourcesJson,
                    object : TypeToken<List<String>>() {}.type
                )
                entity.toRiskZone(coordinates, sources)
            }
        }
    }

    suspend fun getZoneById(id: String): Result<RiskZone> {
        return try {
            val entity = riskZoneDao.getZoneById(id)
            if (entity != null) {
                val coordinates = gson.fromJson<List<List<Double>>>(
                    entity.coordinatesJson,
                    object : TypeToken<List<List<Double>>>() {}.type
                )
                val sources = gson.fromJson<List<String>>(
                    entity.sourcesJson,
                    object : TypeToken<List<String>>() {}.type
                )
                Result.Success(entity.toRiskZone(coordinates, sources))
            } else {
                Result.Error(Exception("Zone not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting zone by id")
            Result.Error(e)
        }
    }

    suspend fun refreshZones(): Result<Unit> {
        // In production, this would fetch from API
        // For now, we use mock data already in database
        return Result.Success(Unit)
    }
}