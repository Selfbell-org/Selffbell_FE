import java.io.FileInputStream
import java.util.Properties

// Load local.properties
val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt) // Hilt 사용을 위해 kapt 플러그인 추가
    alias(libs.plugins.hilt.android) // Hilt Gradle 플러그인 추가
}
// app/build.gradle.kts
kapt {
    correctErrorTypes = true
    arguments {
        // Hilt fastInit 옵션을 추가합니다.
        arg("dagger.fastInit", "true")
    }
}
android {
    namespace = "com.selfbell.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.selfbell.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Manifest Placeholder 설정
        addManifestPlaceholders(mapOf("NAVER_MAPS_CLIENT_ID" to properties.getProperty("NAVER_MAPS_CLIENT_ID")))
        resValue("string", "naver_maps_client_id_from_gradle", properties.getProperty("NAVER_MAPS_CLIENT_ID"))

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
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        compose = true // Compose 사용 설정
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() // libs.versions에서 가져옴
    }
}

dependencies {

    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":feature:home"))
    implementation(project(":feature:alerts"))
    implementation(project(":feature:emergency"))
    implementation(project(":feature:escort"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:auth"))


    // Android 기본 라이브러리!
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.test.manifest)

    // Compose Navigation (최상위 내비게이션)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    //Naver Maps api
    implementation("com.naver.maps:map-sdk:3.22.1")

    // 테스트 관련
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}