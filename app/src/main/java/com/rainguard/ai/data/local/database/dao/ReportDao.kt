package com.rainguard.ai.data.local.database.dao

import androidx.room.*
import com.rainguard.ai.data.local.database.entities.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE uploaded = 0")
    suspend fun getUnuploadedReports(): List<ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity)

    @Update
    suspend fun update(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteById(reportId: String)

    @Query("DELETE FROM reports")
    suspend fun deleteAll()
}