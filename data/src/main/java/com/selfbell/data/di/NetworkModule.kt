package com.selfbell.data.di


import com.selfbell.data.BuildConfig
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.HomeService
import com.selfbell.data.api.NaverApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://your-backend-api-url.com/") // ⚠️ 백엔드 API의 기본 URL로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideHomeService(retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
    }

    @Singleton
    @Provides
    fun provideNaverApiService(retrofit: Retrofit): NaverApiService {
        return retrofit.create(NaverApiService::class.java)
    }

    // BuildConfig를 통해 네이버 API 클라이언트 ID를 제공
    @Provides
    @Singleton
    @Named("X-NCP-APIGW-API-KEY-ID")
    fun provideNaverApiClientId(): String {
        return BuildConfig.NAVER_API_CLIENT_ID
    }

    // BuildConfig를 통해 네이버 API 클라이언트 시크릿을 제공
    @Provides
    @Singleton
    @Named("X-NCP-APIGW-API-KEY")
    fun provideNaverApiClientSecret(): String {
        return BuildConfig.NAVER_API_CLIENT_SECRET
    }
}
