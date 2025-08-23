plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.selfbell.feauter.escort"
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
    // domain 모듈 의존성 (필수) !!
    implementation(project(":domain"))
    implementation(project(":core"))
    //implementation(project(":app"))
    implementation(project(":data"))
    implementation(project(":feature:settings"))

    //네이버
    implementation("com.naver.maps:map-sdk:3.22.1")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.2.0")

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
    kapt(libs.room.compiler)
    implementation(libs.room.ktx) // 코루틴 지원

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // 테스트 관련 (필요시)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}