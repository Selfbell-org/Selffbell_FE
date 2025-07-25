plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
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

}