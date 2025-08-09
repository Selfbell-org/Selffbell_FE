package com.selfbell.data.di

import com.selfbell.data.api.NaverApiService
import com.selfbell.data.repository.impl.AddressRepositoryImpl
import com.selfbell.domain.repository.AddressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverApiService(retrofit: Retrofit): NaverApiService {
        return retrofit.create(NaverApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(repository: AddressRepositoryImpl): AddressRepository {
        return repository
    }
}