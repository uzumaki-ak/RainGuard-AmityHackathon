package com.rainguard.ai.data.local.database.dao

import androidx.room.*
import com.rainguard.ai.data.local.database.entities.RiskZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskZoneDao {
    @Query("SELECT * FROM risk_zones")
    fun getAllZones(): Flow<List<RiskZoneEntity>>

    @Query("SELECT * FROM risk_zones WHERE id = :id")
    suspend fun getZoneById(id: String): RiskZoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<RiskZoneEntity>)

    @Query("DELETE FROM risk_zones")
    suspend fun deleteAll()
}