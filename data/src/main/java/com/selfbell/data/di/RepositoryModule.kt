package com.selfbell.data.di

import com.selfbell.data.repository.impl.AddressRepositoryImpl
import com.selfbell.data.repository.impl.AuthRepositoryImpl
import com.selfbell.data.repository.impl.ContactRepositoryImpl
import com.selfbell.data.repository.impl.HomeRepositoryImpl
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.domain.repository.ContactRepository
import com.selfbell.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// 📌 object를 abstract class로 변경
abstract class RepositoryModule {

    // 📌 @Binds 함수로 변경
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    // 📌 @Binds 함수로 변경
    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    // 📌 @Binds 함수로 변경
    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    // 📌 @Binds 함수로 변경
    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}