package com.selfbell.core.navigation

import android.net.Uri

/**
 * 앱의 모든 내비게이션 경로를 정의하는 객체.
 * 각 경로는 고유한 문자열 ID를 가집니다.
 */
object AppRoute {
    // 초기 화면
    const val SPLASH_ROUTE = "splash_route"   // 설정/프로필 화면
    // 메인 탭 화면 경로
    const val HOME_ROUTE = "home_route"           // 홈 화면
    const val ALERTS_ROUTE = "alerts_route"       // 알림 목록 화면
    const val ESCORT_ROUTE = "escort_route"       // 동행 매칭/그룹 관리 화면
    const val SETTINGS_ROUTE = "settings_route"   // 설정/프로필 화면
    const val FRIENDS_ROUTE = "friends_route"   // 설정/프로필 화면

    //로그인 관련 경로
    const val PROFILE_REGISTER_ROUTE = "profile_register_route"
    const val PERMISSION_ROUTE = "permission_route"
    const val LANDING_ROUTE = "landing_route" // 로그인/회원가입
    const val LOGIN_ROUTE = "login_route" // 로그인
    const val ADDRESS_REGISTER_ROUTE = "address_register_route" // 주소 등록
    const val MAIN_ADDRESS_SETUP_ROUTE = "main_address_setup_route" // 주소 등록
    const val MAIN_ADDRESS_SETUP_ROUTE_WITH_ARGS = "main_address_setup_route/{address}/{lat}/{lng}" // 주소 등록 지정
    fun mainAddressSetupRoute(address: String, lat: Float, lng: Float): String {
        // URL 인코딩을 통해 특수문자 문제를 해결합니다.
        val encodedAddress = Uri.encode(address)
        return "main_address_setup_route/$encodedAddress/$lat/$lng"
    }
    const val CONTACT_REGISTER_ROUTE = "contact_register_route" // 보호자 연락처 등록
    const val ONBOARDING_COMPLETE_ROUTE = "onboarding_complete_route" // 온보딩 완료

    // 그 외 상세 화면 경로 (예시)
    const val ALERT_DETAIL_ROUTE = "alert_detail_route/{alertId}" // 알림 상세 (ID를 인자로 받음)
    const val EMERGENCY_CALL_ROUTE = "emergency_call_route"     // 112 신고 화면
    const val ADDRESS_MANAGEMENT_ROUTE = "address_management_route" // 주소 관리 화면

    const val ONBOARDING_ROUTE = "onboarding_route" // 온보딩

    const val REUSABEL_MAP = "reusable_map" // 지도
    const val PERMISSTION_ROUTE = "permission_route" // 권한

}