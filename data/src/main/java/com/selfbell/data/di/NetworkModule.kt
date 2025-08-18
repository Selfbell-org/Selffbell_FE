package com.selfbell.data.di

import com.selfbell.data.api.AuthService
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.EmergencyBellApi
import com.selfbell.data.api.HomeService
import com.selfbell.data.api.SafeWalksApi
import com.selfbell.data.repository.impl.EmergencyBellRepositoryImpl
import com.selfbell.data.repository.impl.SafeWalkRepositoryImpl
import com.selfbell.domain.repository.EmergencyBellRepository
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://3.37.244.247:8080"

    @Provides
    @Singleton
    @Named("backendOkHttpClient") // 📌 OkHttpClient에 이름 지정
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    @Named("backendRetrofit")
    fun provideRetrofit(@Named("backendOkHttpClient") okHttpClient: OkHttpClient): Retrofit { // 📌 이름으로 주입받음
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthService(@Named("backendRetrofit") retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideContactService(@Named("backendRetrofit") retrofit: Retrofit): ContactService {
        return retrofit.create(ContactService::class.java)
    }
    @Singleton
    @Provides
    fun provideHomeService(@Named("backendRetrofit") retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
    }

    // 안심벨 API 관련 코드 추가
    @Provides
    @Singleton
    fun provideEmergencyBellApi(@Named("backendRetrofit") retrofit: Retrofit): EmergencyBellApi {
        return retrofit.create(EmergencyBellApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEmergencyBellRepository(api: EmergencyBellApi): EmergencyBellRepository {
        return EmergencyBellRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideSafeWalksApi(@Named("backendRetrofit") retrofit: Retrofit): SafeWalksApi {
        return retrofit.create(SafeWalksApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSafeWalkRepository(impl: SafeWalkRepositoryImpl): SafeWalkRepository {
        return impl
    }
}