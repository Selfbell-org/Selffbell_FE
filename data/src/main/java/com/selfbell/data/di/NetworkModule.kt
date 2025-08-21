package com.selfbell.data.di

import com.selfbell.data.api.AuthInterceptor
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.CriminalApi
import com.selfbell.data.api.EmergencyBellApi
import com.selfbell.data.api.FavoriteAddressService
import com.selfbell.data.api.SafeWalksApi
import com.selfbell.data.repository.impl.CriminalRepositoryImpl
import com.selfbell.data.repository.impl.EmergencyBellRepositoryImpl
import com.selfbell.data.repository.impl.FavoriteAddressRepositoryImpl
import com.selfbell.data.repository.impl.TokenManager
import com.selfbell.domain.repository.CriminalRepository
import com.selfbell.domain.repository.EmergencyBellRepository
import com.selfbell.domain.repository.FavoriteAddressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://3.37.244.247:8080/"

    /**
     * AuthInterceptor가 토큰 재발급 시에만 사용하는 별도의 OkHttpClient
     * 이 클라이언트는 AuthInterceptor를 포함하지 않아 순환 참조를 방지합니다.
     */
    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun provideAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * AuthInterceptor가 토큰 재발급에 사용하는 별도의 Retrofit 인스턴스
     */
    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(@Named("authOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * AuthInterceptor가 토큰 재발급에 사용하는 AuthService
     */
    @Provides
    @Singleton
    @Named("authServiceForInterceptor")
    fun provideAuthServiceForInterceptor(@Named("authRetrofit") retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    /**
     * AuthInterceptor 제공
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        @Named("authServiceForInterceptor") authService: AuthService
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, authService)
    }

    /**
     * 일반 API 요청에 사용되는 OkHttpClient (AuthInterceptor 포함)
     */
    @Provides
    @Singleton
    @Named("backendOkHttpClient")
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * 일반 API 요청에 사용되는 Retrofit 인스턴스
     */
    @Singleton
    @Provides
    @Named("backendRetrofit")
    fun provideRetrofit(@Named("backendOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
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

    // 범죄자 API 관련 코드 추가
    @Provides
    @Singleton
    fun provideCriminalApi(@Named("backendRetrofit") retrofit: Retrofit): CriminalApi {
        return retrofit.create(CriminalApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCriminalRepository(api: CriminalApi): CriminalRepository {
        return CriminalRepositoryImpl(api)
    }

    // SafeWalks API 관련 코드 추가
    @Provides
    @Singleton
    fun provideSafeWalksApi(@Named("backendRetrofit") retrofit: Retrofit): SafeWalksApi {
        return retrofit.create(SafeWalksApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAddressService(@Named("backendRetrofit") retrofit: Retrofit): FavoriteAddressService
     {
        return retrofit.create(FavoriteAddressService::class.java)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(impl: FavoriteAddressRepositoryImpl): FavoriteAddressRepository {
        return impl
    }
}