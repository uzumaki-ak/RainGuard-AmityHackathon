package com.rainguard.ai.di

import android.content.Context
import androidx.room.Room
import com.rainguard.ai.data.local.database.RainGuardDatabase
import com.rainguard.ai.data.local.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RainGuardDatabase {
        return Room.databaseBuilder(
            context,
            RainGuardDatabase::class.java,
            "rainguard_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRiskZoneDao(database: RainGuardDatabase): RiskZoneDao = database.riskZoneDao()

    @Provides
    fun provideShelterDao(database: RainGuardDatabase): ShelterDao = database.shelterDao()

    @Provides
    fun provideReportDao(database: RainGuardDatabase): ReportDao = database.reportDao()

    @Provides
    fun provideAlertDao(database: RainGuardDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideRouteDao(database: RainGuardDatabase): RouteDao = database.routeDao()

    @Provides
    fun provideEmergencyContactDao(database: RainGuardDatabase): EmergencyContactDao =
        database.emergencyContactDao()
}