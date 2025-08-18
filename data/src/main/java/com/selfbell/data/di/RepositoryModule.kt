// File: com.selfbell.data.di.RepositoryModule.kt
// (기존 파일에 이 내용을 추가하세요)

package com.selfbell.data.di

import com.selfbell.data.repository.impl.AuthRepositoryImpl
import com.selfbell.data.repository.impl.HomeRepositoryImpl
import com.selfbell.data.repository.impl.AddressRepositoryImpl // AddressRepositoryImpl 임포트 추가
import com.selfbell.domain.HomeRepository
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.domain.repository.AddressRepository
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

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    // **추가된 코드: AddressRepository 바인딩**
    @Binds
    @Singleton
    abstract fun bindAddressRepository(
        addressRepositoryImpl: AddressRepositoryImpl
    ): AddressRepository
}