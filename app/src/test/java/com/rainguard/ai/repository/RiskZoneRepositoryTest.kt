package com.rainguard.ai.repository

import com.google.gson.Gson
import com.rainguard.ai.data.local.database.dao.RiskZoneDao
import com.rainguard.ai.data.local.database.entities.RiskZoneEntity
import com.rainguard.ai.data.repository.RiskZoneRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RiskZoneRepositoryTest {

    private lateinit var riskZoneDao: RiskZoneDao
    private lateinit var repository: RiskZoneRepository
    private val gson = Gson()

    @Before
    fun setup() {
        riskZoneDao = mockk()
        repository = RiskZoneRepository(riskZoneDao, gson)
    }

    @Test
    fun `getRiskZones returns mapped zones`() = runTest {
        val entity = RiskZoneEntity(
            id = "z1",
            name = "Test Zone",
            severity = "high",
            confidence = 0.87f,
            coordinatesJson = "[[77.1,28.7],[77.2,28.7]]",
            sourcesJson = "[\"satellite\"]",
            updated = "2026-02-11T10:00:00Z",
            reason = "Test reason"
        )

        coEvery { riskZoneDao.getAllZones() } returns flowOf(listOf(entity))

        val zones = repository.getRiskZones().first()

        assertEquals(1, zones.size)
        assertEquals("Test Zone", zones[0].name)
    }
}