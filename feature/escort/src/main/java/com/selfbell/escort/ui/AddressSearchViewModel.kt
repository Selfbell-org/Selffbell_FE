package com.selfbell.escort.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressSearchViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AddressModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // 지도 확인 단계로 넘어갈 때 사용할 선택된 주소 정보
    private val _selectedAddressForConfirmation = MutableStateFlow<AddressModel?>(null)
    val selectedAddressForConfirmation = _selectedAddressForConfirmation.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // 간단한 디바운싱 로직 (실제로는 .debounce() 사용 권장)
        viewModelScope.launch {
            if (query.length > 1) {
                try {
                    _searchResults.value = addressRepository.searchAddress(query)
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    // 검색 결과 목록에서 특정 주소를 선택했을 때 호출
    fun selectAddressForConfirmation(address: AddressModel) {
        _selectedAddressForConfirmation.value = address
    }

    // 지도 확인 화면에서 뒤로가기 시 다시 검색 목록으로
    fun clearConfirmation() {
        _selectedAddressForConfirmation.value = null
    }
}