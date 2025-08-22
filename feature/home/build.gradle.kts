plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android) // Hilt Gradle 플러그인 추가

}

android {
    namespace = "com.selfbell.feature.home"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    // domain 모듈 의존성 (필수)
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":data"))
    //Naver Maps api
    implementation("com.naver.maps:map-sdk:3.22.1")


    //permission
    implementation("com.google.accompanist:accompanist-permissions:0.30.1") // 예시 버전, 최신 확인

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
    implementation(libs.androidx.navigation.compose)
    // Networking (Retrofit, OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson) // JSON 파싱 (Gson 사용 시)
    implementation(libs.okhttp.logging.interceptor) // API 로깅

    // Local Database (Room)
    implementation(libs.room.runtime)
    implementation(libs.play.services.maps)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx) // 코루틴 지원

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Naver Maps api
    implementation("com.naver.maps:map-sdk:3.22.1")
    implementation("androidx.compose.material:material:1.6.8") // 최신 버전으로 변경 가능

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // 테스트 관련 (필요시) !!
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}