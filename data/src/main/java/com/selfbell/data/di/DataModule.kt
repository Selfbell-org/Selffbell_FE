package com.selfbell.data.di

import android.content.Context
import android.content.pm.PackageManager
import com.selfbell.data.api.NaverApiService
import com.selfbell.data.repository.impl.AddressRepositoryImpl
import com.selfbell.domain.repository.AddressRepository
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
    fun provideOkHttpClient(
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
            .addInterceptor(headerInterceptor) // 헤더 인터셉터 추가
            .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit { // OkHttpClient를 주입받도록 수정
        return Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/")
            .client(okHttpClient) // 생성한 OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverApiService(retrofit: Retrofit): NaverApiService {
        return retrofit.create(NaverApiService::class.java)
    }

    // AddressRepository 제공 방식 수정 (Impl 클래스를 직접 반환하지 않고, 인터페이스로 바인딩하는 것이 일반적이나,
    // 현재 구조에서는 Impl을 직접 받고 있으므로 그대로 두되, Hilt에서는 @Binds를 더 선호합니다.)
    // 이 부분은 AddressRepositoryImpl 생성자에 @Inject가 있으므로 Hilt가 자동으로 인스턴스화 가능하여
    // 명시적인 provideAddressRepository 함수가 필요 없을 수도 있습니다.
    // AddressRepositoryImpl에 @Inject constructor가 있으므로 Hilt가 생성 방법을 알고 있습니다.
    // 따라서 AddressRepository에 대한 바인딩만 필요합니다.
    // 아래와 같이 변경하거나, abstract class DataModule { @Binds ... } 형태로 변경 가능합니다.
    @Provides
    @Singleton
    fun provideAddressRepository(
        naverApiService: NaverApiService,
        @Named("X-NCP-APIGW-API-KEY-ID") clientId: String, // 여기서 주입받을 이름을 지정
        @Named("X-NCP-APIGW-API-KEY") clientSecret: String // 여기서 주입받을 이름을 지정
    ): AddressRepository {
        // AddressRepositoryImpl 생성자에 @Inject가 있으므로 Hilt가 naverApiService를 주입해줍니다.
        // clientId와 clientSecret도 아래 @Provides 함수들을 통해 주입됩니다.
        return AddressRepositoryImpl(naverApiService, clientId, clientSecret)
    }


    // --- Naver API 키 제공 로직 (Manifest Meta-data 사용) ---
    @Provides
    @Named("X-NCP-APIGW-API-KEY-ID") // AddressRepositoryImpl에서 사용할 이름과 일치
    @Singleton
    fun provideNaverApiClientId(@ApplicationContext context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.selfbell.data.NAVER_MAPS_CLIENT_ID", "DEFAULT_ID_IF_NOT_FOUND")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "DEFAULT_ID_ON_ERROR" // 오류 발생 시 기본값
        } ?: "DEFAULT_ID_IF_NULL" // metaData.getString이 null을 반환할 경우
    }

    @Provides
    @Named("X-NCP-APIGW-API-KEY") // AddressRepositoryImpl에서 사용할 이름과 일치
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
