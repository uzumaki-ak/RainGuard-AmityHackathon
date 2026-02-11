package com.rainguard.ai.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rainguard.ai.data.local.MockDataInitializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideMockDataInitializer(
        @ApplicationContext context: Context,
        database: com.rainguard.ai.data.local.database.RainGuardDatabase,
        gson: Gson
    ): MockDataInitializer = MockDataInitializer(context, database, gson)
}