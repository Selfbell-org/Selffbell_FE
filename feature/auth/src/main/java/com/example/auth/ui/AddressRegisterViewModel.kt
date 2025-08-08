package com.example.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressRegisterViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _searchAddress = MutableStateFlow("")
    val searchAddress = _searchAddress.asStateFlow()

    private val _addressResults = MutableStateFlow<List<AddressModel>>(emptyList())
    val addressResults = _addressResults.asStateFlow()

    private val _isAddressSelected = MutableStateFlow(false)
    val isAddressSelected = _isAddressSelected.asStateFlow()

    fun updateSearchAddress(query: String) {
        _searchAddress.value = query
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                val results = addressRepository.searchAddress(query)
                _addressResults.value = results
            }
        } else {
            _addressResults.value = emptyList()
        }
    }

    fun selectAddress(address: AddressModel) {
        _searchAddress.value = address.roadAddress // 선택된 주소를 검색창에 표시
        _isAddressSelected.value = true
    }

    fun resetSelection() {
        _isAddressSelected.value = false
        _searchAddress.value = ""
        _addressResults.value = emptyList()
    }
}