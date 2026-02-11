package com.rainguard.ai.data.repository

import com.rainguard.ai.data.local.database.dao.ShelterDao
import com.rainguard.ai.data.local.database.entities.ShelterEntity
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.model.Shelter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelterRepository @Inject constructor(
    private val shelterDao: ShelterDao
) {
    fun getShelters(): Flow<List<Shelter>> {
        return shelterDao.getAllShelters().map { entities ->
            entities.map { it.toShelter() }
        }
    }

    suspend fun getShelterById(id: String): Result<Shelter> {
        return try {
            val entity = shelterDao.getShelterById(id)
            if (entity != null) {
                Result.Success(entity.toShelter())
            } else {
                Result.Error(Exception("Shelter not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting shelter by id")
            Result.Error(e)
        }
    }

    suspend fun refreshShelters(): Result<Unit> {
        // In production, fetch from API
        return Result.Success(Unit)
    }

    suspend fun updateShelter(shelter: Shelter) {
        shelterDao.updateShelter(ShelterEntity(
            id = shelter.id,
            name = shelter.name,
            lat = shelter.lat,
            lng = shelter.lng,
            capacity = shelter.capacity,
            available = shelter.available,
            address = shelter.address,
            phone = shelter.phone,
            open = shelter.open,
            wheelchairAccessible = shelter.wheelchairAccessible,
            hasFood = shelter.supplies.food,
            hasWater = shelter.supplies.water,
            hasMedical = shelter.supplies.medical,
            hasBlankets = shelter.supplies.blankets
        ))
    }
}