package com.rainguard.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rainguard.ai.data.local.database.dao.*
import com.rainguard.ai.data.local.database.entities.*

@Database(
    entities = [
        RiskZoneEntity::class,
        ShelterEntity::class,
        ReportEntity::class,
        AlertEntity::class,
        RouteEntity::class,
        EmergencyContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RainGuardDatabase : RoomDatabase() {
    abstract fun riskZoneDao(): RiskZoneDao
    abstract fun shelterDao(): ShelterDao
    abstract fun reportDao(): ReportDao
    abstract fun alertDao(): AlertDao
    abstract fun routeDao(): RouteDao
    abstract fun emergencyContactDao(): EmergencyContactDao
}