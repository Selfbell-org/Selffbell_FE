// domain/build.gradle.kts

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm) // 'jetbrains' 대신 'kotlin'을 사용
    alias(libs.plugins.kotlin.kapt) // kapt 사용을 위해 추가
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // domain 모듈은 순수 Kotlin 로직이므로 AndroidX나 다른 모듈에 의존하지 않습니다.
    // 필요한 경우 Kotlin 표준 라이브러리만 추가합니다.
    implementation(libs.kotlinx.coroutines.core)

    // Hilt 컴파일러를 domain 모듈에서 사용한다면 추가
    // UseCase나 Repository 인터페이스에 Hilt 어노테이션을 사용할 경우 필요
    // 그렇지 않다면 이 두 줄은 제거 가능합니다.
    implementation(libs.hilt.android) // Hilt 어노테이션 (예: @Inject) 사용을 위해
    kapt(libs.hilt.compiler)          // Hilt 코드 생성을 위해
}