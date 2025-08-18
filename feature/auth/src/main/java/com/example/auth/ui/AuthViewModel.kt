package com.selfbell.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.selfbell.domain.repository.AuthRepository
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signUp(name: String, phoneNumber: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                authRepository.signUp(
                    name = name,
                    phoneNumber = phoneNumber,
                    password = password
                )

                _uiState.value = AuthUiState.Success
                Log.d("AuthViewModel", "회원가입 성공. UI 상태 변경: Success")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _uiState.value = AuthUiState.Error(errorMessage)
                Log.e("AuthViewModel", "회원가입 실패. UI 상태 변경: Error - $errorMessage")
            }
        }
    }

    // 📌 로그인 API 호출 로직 추가
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
    //일단 임시로 넘어갈게요
    fun bypassSignUp() {
        _uiState.value = AuthUiState.Success
    }
}