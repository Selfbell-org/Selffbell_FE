package com.example.auth.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainAddressSetupState(
    val address: String = "",
    val addrType: String = "집",
    val userLatLng: LatLng? = null,
    val isAddressSet: Boolean = false
)

@HiltViewModel
class MainAddressSetupViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialAddress = savedStateHandle.get<String>("address")
    private val initialLat = savedStateHandle.get<Float>("lat")
    private val initialLng = savedStateHandle.get<Float>("lng")

    private val initialLatLng = if (initialLat != null && initialLng != null) {
        LatLng(initialLat.toDouble(), initialLng.toDouble())
    } else {
        null
    }

    private val _uiState = MutableStateFlow(
        MainAddressSetupState(
            address = initialAddress ?: "",
            userLatLng = initialLatLng
        )
    )
    val uiState: StateFlow<MainAddressSetupState> = _uiState.asStateFlow()

    fun updateAddress(newAddress: String) {
        _uiState.update { it.copy(address = newAddress) }
    }

    fun updateAddrType(newType: String) {
        _uiState.update { it.copy(addrType = newType) }
    }

    fun updateUserLatLng(newLatLng: LatLng?) {
        _uiState.update { it.copy(userLatLng = newLatLng) }
    }

    fun setMainAddress() {
        viewModelScope.launch {
            _uiState.value.let { state ->
                if (state.address.isNotBlank() && state.userLatLng != null) {
                    // TODO: 주소 저장 로직 구현 (예: 레포지토리 호출)
                    // repository.saveMainAddress(state.address, state.addrType, state.userLatLng)
                    _uiState.update { it.copy(isAddressSet = true) }
                }
            }
        }
    }
}