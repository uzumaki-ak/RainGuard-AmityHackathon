package com.rainguard.ai.data.local.database.dao

import androidx.room.*
import com.rainguard.ai.data.local.database.entities.ShelterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelterDao {
    @Query("SELECT * FROM shelters")
    fun getAllShelters(): Flow<List<ShelterEntity>>

    @Query("SELECT * FROM shelters WHERE id = :id")
    suspend fun getShelterById(id: String): ShelterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelters: List<ShelterEntity>)

    @Update
    suspend fun updateShelter(shelter: ShelterEntity)

    @Query("DELETE FROM shelters")
    suspend fun deleteAll()
}