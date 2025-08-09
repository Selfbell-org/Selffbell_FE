// feature/settings/ui/SettingsViewModel.kt
package com.selfbell.settings.ui

import androidx.lifecycle.ViewModel
import com.selfbell.core.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    // 프로필 정보 상태
    private val _profileName = MutableStateFlow("홍길동")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileImageRes = MutableStateFlow(R.drawable.default_profile_icon2)
    val profileImageRes: StateFlow<Int> = _profileImageRes.asStateFlow()

    // 알림 설정 상태
    private val _alertEnabled = MutableStateFlow(true)
    val alertEnabled: StateFlow<Boolean> = _alertEnabled.asStateFlow()

    // 알림 설정 토글 함수
    fun setAlertEnabled(enabled: Boolean) {
        _alertEnabled.value = enabled
        // TODO: 알림 설정 상태를 데이터베이스에 저장하는 로직 추가
    }

    // 프로필 관리 화면에서 돌아왔을 때 프로필을 업데이트하는 함수
    fun updateProfile(name: String, imageRes: Int) {
        _profileName.value = name
        _profileImageRes.value = imageRes
        // TODO: 프로필 정보를 데이터베이스에 저장하는 로직 추가
    }

    // 로그아웃 로직 (예시)
    fun logout() {
        // TODO: 사용자 인증 정보 삭제 및 로그인 화면으로 이동하는 로직 추가
    }
}