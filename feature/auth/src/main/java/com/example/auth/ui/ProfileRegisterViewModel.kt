package com.example.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 회원가입 관련 UI 상태를 관리하는 데이터 클래스
data class ProfileRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileRegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository // Hilt를 통해 AuthRepository 주입
) : ViewModel() {

    // UI 상태를 노출하는 StateFlow
    private val _uiState = MutableStateFlow(ProfileRegisterUiState())
    val uiState: StateFlow<ProfileRegisterUiState> = _uiState.asStateFlow()

    /**
     * 회원가입 API를 호출하는 함수.
     * ProfileRegisterScreen에서 호출됩니다.
     */
    fun signUp(name: String, phoneNumber: String, password: String) {
        // 중복 호출 방지
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // AuthRepository의 signUp 함수를 호출
                authRepository.signUp(name, phoneNumber, password)

                // API 호출 성공 시 상태 업데이트
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                Log.d("ProfileRegisterVM", "회원가입 성공. 다음 화면으로 이동 준비.")

            } catch (e: Exception) {
                // API 호출 실패 시 상태 업데이트
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e("ProfileRegisterVM", "회원가입 실패: ${e.message}")
            }
        }
    }
}