package com.example.auth.ui


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng // LatLng import
import com.selfbell.core.location.LocationTracker
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.ReverseGeocodingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.isNotBlank

@HiltViewModel
class AddressRegisterViewModel @Inject constructor(
    private val addressRepository: AddressRepository, // Hilt를 통해 주입
    private val locationTracker: LocationTracker, // 👈 [추가]
    private val reverseGeocodingRepository: ReverseGeocodingRepository
) : ViewModel() {

    private val _searchAddress = MutableStateFlow("")
    val searchAddress = _searchAddress.asStateFlow()

    private val _addressResults = MutableStateFlow<List<AddressModel>>(emptyList())
    val addressResults = _addressResults.asStateFlow()

    private val _isAddressSelected = MutableStateFlow(false)
    val isAddressSelected = _isAddressSelected.asStateFlow()

    // 선택된 주소의 좌표를 저장
    private val _selectedLatLng = MutableStateFlow<LatLng?>(null)
    val selectedLatLng = _selectedLatLng.asStateFlow()

    private val _selectedAddressDetail = MutableStateFlow<AddressModel?>(null)
    val selectedAddressDetail = _selectedAddressDetail.asStateFlow()

    // 디바운싱을 위한 Job
    private var searchJob: Job? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        // searchAddress StateFlow의 변경을 감지하고 debounce 적용
        Log.d("geocoding", "AddressRegisterViewModel initialized")
        viewModelScope.launch {
            _searchAddress
                .debounce(500L) // 0.5초 동안 입력이 없으면 API 호출
                .filter { query ->
                    // 너무 짧은 검색어는 무시하거나, 여기서 초기 상태(결과 없음, 선택 안됨)로 되돌릴 수 있음
                    query.isNotBlank() && query.length > 1 // 예: 2글자 이상일 때만 검색
                }
                .distinctUntilChanged() // 이전과 동일한 검색어로는 다시 호출하지 않음
                .collectLatest { query -> // 이전 검색 작업이 있다면 취소하고 새 검색 시작
                    Log.d("geocoding", "Debounce collected query: $query")
                    if (!_isAddressSelected.value) { // 주소가 이미 선택된 상태가 아니라면 검색 실행
                        Log.d("geocoding", "Calling performSearch for query: $query, isAddressSelected: ${isAddressSelected.value}")
                        performSearch(query)
                    }
                }
        }

        // 검색창이 비워졌을 때 결과 목록도 초기화하고, 선택 상태도 해제
        viewModelScope.launch {
            _searchAddress.collect { query ->
                if (query.isBlank() && !_isAddressSelected.value) { // 선택되지 않았고, 검색어가 비었을 때
                    _addressResults.value = emptyList()
                    // _isAddressSelected.value = false // 이미 false일 것이므로, 필요하면 추가
                }
                // 주소가 선택된 후 검색창을 수정하기 시작하면 선택 상태를 해제할 수도 있음 (선택적)
                // if (_isAddressSelected.value && query != _selectedAddressDetail.value?.roadAddress /*또는 다른 기준*/) {
                //    resetSelection()
                // }
            }
        }
    }


    fun updateSearchAddress(query: String) {
        Log.d("geocoding", "updateSearchAddress called with query: $query")
        // 주소가 선택된 상태에서 사용자가 다시 입력을 시작하면, 선택 상태를 해제하고 새로 검색할 수 있도록 준비
        if (_isAddressSelected.value && query != (_addressResults.value.firstOrNull { it.roadAddress == _searchAddress.value || it.jibunAddress == _searchAddress.value }?.roadAddress ?: _searchAddress.value) ) { // 또는 다른 비교 로직
            resetSelectionInternal() // 내부적으로 선택 해제, 결과 목록은 비우지 않고 새 검색 준비
        }
        _searchAddress.value = query
        Log.d("geocoding", "New searchAddress value: ${_searchAddress.value}")
        // _isAddressSelected.value = false // 검색어 변경 시 일단 선택 해제 (선택적 UI/UX)
        // _addressResults.value = emptyList() // 검색어 변경 시 이전 결과 즉시 숨기기 (선택적 UI/UX)

    }

    private fun performSearch(query: String) {
        Log.d("geocoding", "performSearch started for query: $query") // <<< 추가된 로그 1
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                Log.d("geocoding", "Calling addressRepository.searchAddress for: $query") // <<< 추가된 로그 2
                // 로딩 상태 표시 (필요하다면)
                // _isLoading.value = true
                val results = addressRepository.searchAddress(query)
                Log.d("geocoding", "Received ${results.size} results from repository for query: $query") // <<< 추가된 로그 3
                _addressResults.value = results
                Log.d("geocoding", "_addressResults updated. Current count: ${_addressResults.value.size}") // <<< 추가된 로그 4
            } catch (e: Exception) {
                Log.e("geocoding", "Error in performSearch for query: $query", e) // <<< 추가된 로그 5 (e: Exception 포함)
                _addressResults.value = emptyList() // 오류 시 빈 목록
                // _errorState.value = "주소 검색 중 오류가 발생했습니다."
            } finally {
                Log.d("geocoding", "performSearch finished for query: $query") // <<< 추가된 로그 6
                // 로딩 상태 해제
                // _isLoading.value = false
            }
        }
    }

    fun selectAddress(address: AddressModel) {
        _searchAddress.value = address.roadAddress.ifEmpty { address.jibunAddress } // TextField에 선택된 주소 표시
        _addressResults.value = listOf(address) // 선택된 주소만 남기거나, 혹은 그대로 둘 수도 있음 (UI/UX 결정)
        _selectedLatLng.value = LatLng(address.y.toDouble(), address.x.toDouble())
        _selectedAddressDetail.value = address // 선택된 주소 상세 정보 저장
        _isAddressSelected.value = true
    }

    // 외부에서 호출 가능한 선택 해제 함수
    fun resetSelection() {
        resetSelectionInternal()
        _searchAddress.value = "" // 검색창도 비움
        _addressResults.value = emptyList() // 결과 목록도 비움
    }

    // 내부적으로 사용될 선택 해제 함수 (검색창 내용, 결과 목록 유지 가능)
    private fun resetSelectionInternal() {
        _isAddressSelected.value = false
        _selectedAddressDetail.value = null
        _selectedLatLng.value = null
        // 지도 초기화 로직
    }


    fun getCurrentLocationAddress() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. 새로 만든 getCurrentLocation() 함수 호출
                val location = locationTracker.getCurrentLocation()
                if (location == null) {
                    Log.e("AddressRegisterVM", "Failed to get current location.")
                    // TODO: 사용자에게 위치를 가져올 수 없다는 Toast 메시지 등을 보여주는 로직 추가
                    return@launch
                }

                // 2. 위도/경도를 주소 문자열로 변환
                val addressString = reverseGeocodingRepository.reverseGeocode(
                    lat = location.latitude,
                    lon = location.longitude
                )
                if (addressString == null) {
                    Log.e("AddressRegisterVM", "Failed to reverse geocode.")
                    // TODO: 사용자에게 주소를 변환할 수 없다는 Toast 메시지 등을 보여주는 로직 추가
                    return@launch
                }

                // 3. 변환된 주소와 좌표로 AddressModel 객체 생성
                val currentAddress = AddressModel(
                    roadAddress = addressString,
                    jibunAddress = "", // 도로명 주소만 사용
                    y = location.latitude.toString(),
                    x = location.longitude.toString()
                )

                // 4. 기존 selectAddress 함수를 재사용하여 UI 상태 업데이트
                selectAddress(currentAddress)

            } catch (e: Exception) {
                Log.e("AddressRegisterVM", "Error getting current location address", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

