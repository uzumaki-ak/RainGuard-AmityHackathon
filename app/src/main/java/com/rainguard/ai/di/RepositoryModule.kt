package com.rainguard.ai.di

import com.rainguard.ai.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // All repositories are already provided via constructor injection
    // This module is kept for future abstract bindings if needed
}