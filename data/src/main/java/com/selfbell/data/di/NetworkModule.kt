package com.selfbell.data.di

import com.selfbell.data.api.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// data/di/NetworkModule.kt

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://your-backend-api-url.com/") // ⚠️ 백엔드 API의 기본 URL로 변경
            .addConverterFactory(GsonConverterFactory.create()) // JSON 변환 라이브러리
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        // Retrofit이 AuthService 인터페이스의 구현체를 자동으로 생성
        return retrofit.create(AuthService::class.java)
    }
}