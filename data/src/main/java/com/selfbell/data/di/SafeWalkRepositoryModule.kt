// data/di/SafeWalkRepositoryModule.kt

package com.selfbell.data.di

import com.selfbell.data.repository.impl.SafeWalkRepositoryImpl
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SafeWalkRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSafeWalkRepository(
        safeWalkRepositoryImpl: SafeWalkRepositoryImpl
    ): SafeWalkRepository
}