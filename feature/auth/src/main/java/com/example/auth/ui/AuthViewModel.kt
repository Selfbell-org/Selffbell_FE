package com.example.auth.ui

// auth/AuthViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Hilt를 사용해 의존성 주입 (선택 사항)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository // API 통신을 담당하는 리포지토리
) : ViewModel() {

    // UI 상태를 관리하는 StateFlow
    // 현재는 간단하게 전화번호만 관리하지만, 로딩, 에러 등 다양한 상태를 추가할 수 있습니다.
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    // 회원가입 성공 여부를 나타내는 상태
    private val _isSignUpSuccessful = MutableStateFlow(false)
    val isSignUpSuccessful = _isSignUpSuccessful.asStateFlow()

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    // 회원가입 비즈니스 로직
    fun signUp(password: String) {
        viewModelScope.launch {
            try {
                // 1. 회원가입 API 호출
                val result = authRepository.signUp(
                    phoneNumber = _phoneNumber.value,
                    password = password
                )

                // 2. API 호출 결과에 따라 상태 업데이트
                if (result.isSuccess) {
                    _isSignUpSuccessful.value = true
                } else {
                    // 에러 처리: 실패 시 에러 메시지 등을 상태에 저장할 수 있습니다.
                }
            } catch (e: Exception) {
                // 예외 처리: 네트워크 에러 등
            }
        }
    }
}