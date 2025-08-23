package com.selfbell.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.data.repository.impl.FCMTokenManager
import javax.inject.Inject
import com.selfbell.domain.model.Profile // ✅ Profile 모델 import

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fcmTokenManager: FCMTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()
    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    // ✅ 프로필 업데이트를 위한 상태
    private val _profileUpdateState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val profileUpdateState = _profileUpdateState.asStateFlow()

    fun signUp(name: String, phoneNumber: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        // ✅ 유효성 검사 추가: 입력값이 비어있으면 에러 반환
        if (name.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("모든 필드를 입력해야 합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val deviceToken = fcmTokenManager.getFCMToken() ?: "deviceToken2"
                Log.d("AuthViewModel", "FCM 토큰 가져오기: $deviceToken")

                authRepository.signUp(
                    deviceToken = deviceToken,
                    deviceType = "ANDROID",
                    name = name,
                    phoneNumber = phoneNumber,
                    password = password
                )

                authRepository.login(
                    phoneNumber = phoneNumber,
                    password = password,
                    deviceToken = deviceToken,
                    deviceType = "ANDROID"
                )

                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "회원가입 및 자동 로그인 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "회원가입/로그인 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val profile = authRepository.getUserProfile()
                _userName.value = profile?.name
                Log.d("AuthViewModel", "사용자 프로필 불러오기 성공: ${profile?.name}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "사용자 프로필 불러오기 실패", e)
                _userName.value = null
            }
        }
    }

    // ✅ 프로필 업데이트 함수 추가
    fun updateProfile(name: String) {
        if (_profileUpdateState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _profileUpdateState.value = AuthUiState.Loading
            try {
                authRepository.updateProfile(name)
                _profileUpdateState.value = AuthUiState.Success
                _userName.value = name // UI 상태 즉시 업데이트
                Log.d("AuthViewModel", "프로필 이름 변경 성공: $name")
            } catch (e: Exception) {
                _profileUpdateState.value = AuthUiState.Error(e.message ?: "프로필 업데이트 실패")
                Log.e("AuthViewModel", "프로필 업데이트 실패", e)
            }
        }
    }

    fun login(phoneNumber: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        // ✅ 유효성 검사 추가
        if (phoneNumber.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("전화번호와 비밀번호를 입력해야 합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val deviceToken = fcmTokenManager.getFCMToken() ?: "deviceToken" // ✅ 토큰 가져오기
                authRepository.login(
                    phoneNumber = phoneNumber,
                    password = password,
                    deviceToken = deviceToken, // ✅ 파라미터 전달
                    deviceType = "ANDROID" // ✅ 파라미터 전달
                )

                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "로그인 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "로그인 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    // ✅ registerMainAddress 함수 수정: name 파라미터 제거
    fun registerMainAddress(address: String, lat: Double, lon: Double) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.registerMainAddress(address, lat, lon)
                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "메인 주소 등록 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "메인 주소 등록 중 알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "메인 주소 등록 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    fun refreshFCMToken() {
        viewModelScope.launch {
            try {
                val newToken = fcmTokenManager.refreshFCMToken()
                Log.d("AuthViewModel", "FCM 토큰 새로고침 완료: $newToken")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "FCM 토큰 새로고침 실패", e)
            }
        }
    }

    // ✅ 로그아웃 함수 추가
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                // TODO: 로그아웃 후 내비게이션 처리 (예: 로그인 화면으로 이동)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "로그아웃 실패", e)
            }
        }
    }
}