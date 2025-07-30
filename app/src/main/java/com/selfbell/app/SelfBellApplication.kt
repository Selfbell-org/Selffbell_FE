
package com.selfbell.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp // HiltAndroidApp 어노테이션 임포트

/**
 * SelfBell 애플리케이션의 Hilt 진입점.
 * @HiltAndroidApp 어노테이션은 Hilt가 이 Application 클래스를 사용하여
 * 앱의 의존성 그래프를 생성하도록 지시합니다.
 */
@HiltAndroidApp
class SelfBellApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 앱 초기화에 필요한 추가적인 코드 (예: 서드파티 SDK 초기화 등)
        // 이 곳은 앱 프로세스가 생성될 때 가장 먼저 실행되는 지점입니다.
    }
}