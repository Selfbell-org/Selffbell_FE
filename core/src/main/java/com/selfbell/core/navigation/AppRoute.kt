package com.selfbell.core.navigation

import android.net.Uri

object AppRoute {
    // 초기 화면
    const val SPLASH_ROUTE = "splash_route"
    // 메인 탭 화면 경로
    const val HOME_ROUTE = "home_route"
    const val ALERTS_ROUTE = "alerts_route"
    const val ESCORT_ROUTE = "escort_route"
    const val SETTINGS_ROUTE = "settings_route"
    const val FRIENDS_ROUTE = "friends_route"

    // 로그인 관련 경로
    const val PROFILE_REGISTER_ROUTE_WITH_ARGS = "profile_register_route/{phoneNumber}/{password}"
    // 📌 회원가입 화면으로 이동하는 헬퍼 함수 추가
    fun profileRegisterRoute(phoneNumber: String, password: String): String =
        "profile_register_route/$phoneNumber/$password"

    const val PERMISSION_ROUTE = "permission_route" // 📌 오타 수정
    const val LANDING_ROUTE = "landing_route"
    const val LOGIN_ROUTE = "login_route"
    const val PHONE_NUMBER_ROUTE = "phone_number_route"
    const val ADDRESS_REGISTER_ROUTE = "address_register_route"
    const val MAIN_ADDRESS_SETUP_ROUTE = "main_address_setup_route"
    const val MAIN_ADDRESS_SETUP_ROUTE_WITH_ARGS = "main_address_setup_route/{address}/{lat}/{lng}"
    fun mainAddressSetupRoute(address: String, lat: Float, lng: Float): String {
        val encodedAddress = Uri.encode(address)
        return "main_address_setup_route/$encodedAddress/$lat/$lng"
    }
    const val CONTACT_REGISTER_ROUTE = "contact_register_route"
    const val ONBOARDING_COMPLETE_ROUTE = "onboarding_complete_route"
    const val PASSWORD_ROUTE = "password_route"
    const val PASSWORD_ROUTE_WITH_ARGS = "$PASSWORD_ROUTE/{phoneNumber}"
    fun passwordRoute(phoneNumber: String): String =
        "$PASSWORD_ROUTE/$phoneNumber"

    // 그 외 상세 화면 경로 (예시)
    const val ALERT_DETAIL_ROUTE = "alert_detail_route/{alertId}"
    const val EMERGENCY_CALL_ROUTE = "emergency_call_route"
    const val ADDRESS_MANAGEMENT_ROUTE = "address_management_route"
    const val ONBOARDING_ROUTE = "onboarding_route"
    const val REUSABEL_MAP = "reusable_map"
}