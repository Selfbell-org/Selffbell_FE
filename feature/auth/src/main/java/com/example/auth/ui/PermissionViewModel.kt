package com.example.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 각 권한의 상태를 나타내는 데이터 클래스
data class PermissionStateItem(
    val isGranted: Boolean = false,
    val isRequested: Boolean = false
)

// UI의 전체 권한 상태를 나타내는 데이터 클래스
data class PermissionUiState(
    val location: PermissionStateItem = PermissionStateItem(),
    val backgroundLocation: PermissionStateItem = PermissionStateItem(),
    val pushNotification: PermissionStateItem = PermissionStateItem(),
    val contacts: PermissionStateItem = PermissionStateItem()
)

// Hilt를 사용한 ViewModel
@HiltViewModel
class PermissionViewModel @Inject constructor(
    // 추후 권한 상태를 저장하는 리포지토리(Repository) 등을 주입할 수 있습니다.
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState = _uiState.asStateFlow()

    // 위치 권한 상태 업데이트
    fun updateLocationPermission(isGranted: Boolean) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    location = currentState.location.copy(
                        isGranted = isGranted,
                        isRequested = true
                    )
                )
            }
        }
    }

    // 백그라운드 위치 권한 상태 업데이트
    fun updateBackgroundLocationPermission(isGranted: Boolean) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    backgroundLocation = currentState.backgroundLocation.copy(
                        isGranted = isGranted,
                        isRequested = true
                    )
                )
            }
        }
    }

    // 푸시 알림 권한 상태 업데이트
    fun updatePushNotificationPermission(isGranted: Boolean) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    pushNotification = currentState.pushNotification.copy(
                        isGranted = isGranted,
                        isRequested = true
                    )
                )
            }
        }
    }

    // 연락처 권한 상태 업데이트
    fun updateContactsPermission(isGranted: Boolean) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    contacts = currentState.contacts.copy(
                        isGranted = isGranted,
                        isRequested = true
                    )
                )
            }
        }
    }
}