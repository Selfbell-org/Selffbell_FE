package com.selfbell.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.selfbell.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 사용자 인증 관련 UI 상태를 정의하는 sealed interface.
 * UI는 이 상태를 관찰하여 적절한 화면을 표시합니다.
 */
sealed interface AuthUiState {
    // 초기 상태
    object Idle : AuthUiState
    // API 호출 중
    object Loading : AuthUiState
    // API 호출 성공
    object Success : AuthUiState
    // API 호출 실패, 에러 메시지 포함
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // 전화번호 입력 상태
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    // UI 상태를 종합적으로 관리하는 StateFlow
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    fun signUp(password: String) {
        viewModelScope.launch {
            // 회원가입 시작 시 로딩 상태로 변경
            _uiState.value = AuthUiState.Loading

            try {
                // 1. 회원가입 API 호출
                // signUp 함수가 성공하면 예외를 던지지 않고, 실패 시 예외를 던집니다.
                authRepository.signUp(
                    phoneNumber = _phoneNumber.value,
                    password = password
                )

                // 2. 호출 성공 시, 성공 상태로 변경
                _uiState.value = AuthUiState.Success

            } catch (e: Exception) {
                // 3. 호출 실패 시, 에러 상태로 변경
                _uiState.value = AuthUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }
}