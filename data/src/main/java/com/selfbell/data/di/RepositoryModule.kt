package com.selfbell.data.di

// data/di/RepositoryModule.kt


import com.selfbell.data.repository.impl.AuthRepositoryImpl // Data 모듈의 구현체
import com.selfbell.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}