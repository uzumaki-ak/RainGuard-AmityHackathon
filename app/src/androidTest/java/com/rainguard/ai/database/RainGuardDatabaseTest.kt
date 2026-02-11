package com.rainguard.ai.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rainguard.ai.data.local.database.RainGuardDatabase
import com.rainguard.ai.data.local.database.entities.ShelterEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RainGuardDatabaseTest {

    private lateinit var database: RainGuardDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RainGuardDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndReadShelter() = runBlocking {
        val shelter = ShelterEntity(
            id = "test1",
            name = "Test Shelter",
            lat = 28.7,
            lng = 77.1,
            capacity = 100,
            available = 50,
            address = "Test Address",
            phone = "1234567890",
            open = true,
            wheelchairAccessible = true,
            hasFood = true,
            hasWater = true,
            hasMedical = false,
            hasBlankets = true
        )

        database.shelterDao().insertAll(listOf(shelter))

        val shelters = database.shelterDao().getAllShelters().first()
        assertEquals(1, shelters.size)
        assertEquals("Test Shelter", shelters[0].name)
    }
}