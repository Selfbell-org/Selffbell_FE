package com.example.auth.ui


import com.selfbell.core.model.Contact // 로컬 연락처 모델 import

sealed interface ContactUiState {
    object Idle : ContactUiState
    object Loading : ContactUiState
    data class Success(val contacts: List<Contact>) : ContactUiState
    data class Error(val message: String) : ContactUiState
}