import java.util.Properties // Properties 클래스를 사용하기 위해 import 추가

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

// local.properties 파일 로드
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.selfbell.data"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Manifest Placeholders 추가
        manifestPlaceholders["NAVER_MAPS_CLIENT_ID"] = properties.getProperty("NAVER_MAPS_CLIENT_ID", "YOUR_DEFAULT_ID")
        manifestPlaceholders["NAVER_MAPS_CLIENT_SECRET"] = properties.getProperty("NAVER_MAPS_CLIENT_SECRET", "YOUR_DEFAULT_SECRET")

        // Hilt에 주입하기 위해 BuildConfig 필드 추가

        buildConfigField("String", "NAVER_API_CLIENT_ID", "\"${properties.getProperty("NAVER_API_CLIENT_ID", "")}\"")
        buildConfigField("String", "NAVER_API_CLIENT_SECRET", "\"${properties.getProperty("NAVER_API_CLIENT_SECRET", "")}\"")
        resValue("string", "base_url", properties.getProperty("API_BASE_URL", ""))
        resValue("string", "websocket_endpoint", properties.getProperty("WEBSOCKET_ENDPOINT", ""))

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}
dependencies {
    // domain 모듈 의존성 (필수)!
    implementation(project(":domain"))

    // Compose UI
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.test.manifest)


    // Android 기본 라이브러리 !!
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Networking (Retrofit, OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson) // JSON 파싱 (Gson 사용 시)
    implementation(libs.okhttp.logging.interceptor) // API 로깅

    // Local Database (Room)
    implementation(libs.room.runtime)
    implementation(libs.play.services.maps)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx) // 코루틴 지원
    implementation(libs.androidx.datastore.preferences)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    //Stomp 프로토콜
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // Stomp 라이브러리가 사용하는 RxJava 종속성 추가
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")


    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // 테스트 관련 (필요시)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}