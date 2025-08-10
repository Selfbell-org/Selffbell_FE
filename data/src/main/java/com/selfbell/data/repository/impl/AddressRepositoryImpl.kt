package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.NaverApiService
import com.selfbell.data.api.response.NaverGeocodeResponse
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Named

class AddressRepositoryImpl @Inject constructor(
    private val naverApiService: NaverApiService,
    @Named("X-NCP-APIGW-API-KEY-ID") private val clientId: String,
    @Named("X-NCP-APIGW-API-KEY") private val clientSecret: String
) : AddressRepository {
    override suspend fun searchAddress(query: String): List<AddressModel> {
        return try {
            Log.d("AddressSearch", "Requesting geocode for query: $query, ClientID: $clientId") // 요청 직전 로그 추가

            val response = naverApiService.getGeocode(
                query = query,
                count = 3
            )
            if (response.status == "OK" && response.addresses != null) {
                response.addresses.map { apiAddressDTO ->
                    apiAddressDTO.toDomainModel()
                }
            } else {
                // API 오류 처리 (예: 로깅, 사용자에게 알림 등)
                System.err.println("Geocoding API Error: ${response.errorMessage}")
                Log.e("AddressSearch", "Geocoding API Error: Status=${response.status}, Message=${response.errorMessage}")

                emptyList()
            }
        } catch (e: Exception) {
            // 네트워크 오류 등 예외 처리
            Log.e("AddressSearch", "Exception during geocode API call", e) // 예외 발생 시 상세 로그 추가

            e.printStackTrace()
            emptyList()
        }
    }
}
