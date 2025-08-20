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

    // 로그인/회원가입 관련 경로
    const val LANDING_ROUTE = "landing_route"
    const val PHONE_NUMBER_ROUTE = "phone_number_route" // 회원가입용 전화번호 입력
    const val PHONE_NUMBER_LOGIN_ROUTE = "phone_number_login_route" // 📌 로그인용 전화번호 입력
    const val PASSWORD_ROUTE = "password_route" // 회원가입용 비밀번호 입력
    const val PASSWORD_ROUTE_WITH_ARGS = "$PASSWORD_ROUTE/{phoneNumber}"
    fun passwordRoute(phoneNumber: String): String = "$PASSWORD_ROUTE/$phoneNumber"
    const val LOGIN_PIN_ROUTE = "login_pin_route" // 📌 로그인용 PIN 입력
    const val LOGIN_PIN_ROUTE_WITH_ARGS = "$LOGIN_PIN_ROUTE/{phoneNumber}"
    fun loginPinRoute(phoneNumber: String): String = "$LOGIN_PIN_ROUTE/$phoneNumber"
    const val PROFILE_REGISTER_ROUTE = "profile_register_route" // 📌 인자 없는 기본 경로 추가
    const val PROFILE_REGISTER_ROUTE_WITH_ARGS = "$PROFILE_REGISTER_ROUTE/{phoneNumber}/{password}"
    // 📌 헬퍼 함수가 올바른 경로를 생성하도록 수정
    fun profileRegisterRoute(phoneNumber: String, password: String): String =
        "$PROFILE_REGISTER_ROUTE/$phoneNumber/$password"
    const val PERMISSION_ROUTE = "permission_route"
    const val ADDRESS_REGISTER_ROUTE = "address_register_route"
    const val MAIN_ADDRESS_SETUP_ROUTE_WITH_ARGS = "main_address_setup_route/{address}/{lat}/{lng}"
    fun mainAddressSetupRoute(address: String, lat: Float, lng: Float): String {
        val encodedAddress = Uri.encode(address)
        return "main_address_setup_route/$encodedAddress/$lat/$lng"
    }
    const val CONTACT_REGISTER_ROUTE = "contact_register_route"
    const val ONBOARDING_COMPLETE_ROUTE = "onboarding_complete_route"

    // 그 외 상세 화면 경로
    const val ALERT_DETAIL_ROUTE = "alert_detail_route/{alertId}"
    const val EMERGENCY_CALL_ROUTE = "emergency_call_route"
    const val ADDRESS_MANAGEMENT_ROUTE = "address_management_route"
    const val REUSABEL_MAP = "reusable_map"

}