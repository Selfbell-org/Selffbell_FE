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
    // 📌 Add the missing state variable
    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    fun signUp(name: String, phoneNumber: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                // ✅ FCM 토큰 가져오기
                val deviceToken = fcmTokenManager.getFCMToken() ?: "deviceToken2"
                Log.d("AuthViewModel", "FCM 토큰 가져오기: $deviceToken")

                authRepository.signUp(
                    deviceToken = deviceToken,
                    deviceType = "ANDROID",
                    name = name,
                    phoneNumber = phoneNumber,
                    password = password
                )

                // ✅ 회원가입 성공 후 즉시 로그인하여 토큰 저장
                authRepository.login(phoneNumber, password)

                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "회원가입 및 자동 로그인 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "회원가입/로그인 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

//    fun fetchUserProfile() {
//        viewModelScope.launch {
//            try {
//                // ✅ AuthRepository에 구현된 getUserProfile 함수를 호출합니다.
//                val profile = authRepository.getUserProfile()
//                _userName.value = profile.name // ✅ ViewModel 상태 업데이트
//                Log.d("AuthViewModel", "사용자 프로필 불러오기 성공: ${profile.name}")
//            } catch (e: Exception) {
//                Log.e("AuthViewModel", "사용자 프로필 불러오기 실패", e)
//                _userName.value = null // 에러 발생 시 이름 초기화
//            }
//        }
//    }
    fun login(phoneNumber: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                authRepository.login(phoneNumber, password)

                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "로그인 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "로그인 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    // 📌 메인 주소 등록 API 호출 로직 추가
    fun registerMainAddress(name: String, address: String, lat: Double, lon: Double) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.registerMainAddress(name, address, lat, lon)
                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "메인 주소 등록 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "메인 주소 등록 중 알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "메인 주소 등록 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    // 📌 FCM 토큰 새로고침 함수 추가
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
}