package com.selfbell.data.di

import android.content.Context
import android.content.pm.PackageManager
import com.selfbell.data.api.NaverApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @Named("naverOkHttpClient") // 📌 OkHttpClient에 이름 지정
    fun provideNaverOkHttpClient(
        @Named("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Named("X-NCP-APIGW-API-KEY") clientSecret: String
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("naverRetrofit")
    fun provideNaverRetrofit(@Named("naverOkHttpClient") okHttpClient: OkHttpClient): Retrofit { // 📌 이름으로 주입받음
        return Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverApiService(@Named("naverRetrofit") retrofit: Retrofit): NaverApiService {
        return retrofit.create(NaverApiService::class.java)
    }

    @Provides
    @Named("X-NCP-APIGW-API-KEY-ID")
    @Singleton
    fun provideNaverApiClientId(@ApplicationContext context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.selfbell.data.NAVER_MAPS_CLIENT_ID", "DEFAULT_ID_IF_NOT_FOUND")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "DEFAULT_ID_ON_ERROR"
        } ?: "DEFAULT_ID_IF_NULL"
    }

    @Provides
    @Named("X-NCP-APIGW-API-KEY")
    @Singleton
    fun provideNaverApiClientSecret(@ApplicationContext context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.selfbell.data.NAVER_MAPS_CLIENT_SECRET", "DEFAULT_SECRET_IF_NOT_FOUND")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "DEFAULT_SECRET_ON_ERROR"
        } ?: "DEFAULT_SECRET_IF_NULL"
    }
}