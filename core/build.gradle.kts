plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.selfbell.core"
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
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn" // @OptIn 어노테이션 사용 시 필요

    }
    buildFeatures {
        compose = true // Compose 사용 설정
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() // libs.versions에서 가져옴
    }
}

dependencies {

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

    //최상위 네비게이션
    implementation(libs.androidx.navigation.compose)
    // Coroutines!
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt (core 모듈이 Hilt를 사용하거나 DI 모듈이 있다면)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    //Naver Maps api
    implementation("com.naver.maps:map-sdk:3.22.1")

    //permission
    implementation("com.google.accompanist:accompanist-permissions:0.30.1") // 예시 버전, 최신 확인



    // 테스트 관련 (필요시)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}