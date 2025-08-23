package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.NaverReverseGeocodingService
import com.selfbell.data.api.response.NaverReverseGeocodeResponse
import com.selfbell.domain.repository.ReverseGeocodingRepository
import javax.inject.Inject

class ReverseGeocodingRepositoryImpl @Inject constructor(
    private val naverReverseGeocodingService: NaverReverseGeocodingService
) : ReverseGeocodingRepository {

    override suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            Log.d("ReverseGeocodingRepository", "=== Reverse Geocoding 시작 ===")
            Log.d("ReverseGeocodingRepository", "입력 좌표: lat=$lat, lon=$lon")
            
            val coords = "$lon,$lat" // 네이버 API는 "경도,위도" 형식을 요구
            Log.d("ReverseGeocodingRepository", "API 요청 좌표: $coords")
            
            Log.d("ReverseGeocodingRepository", "네이버 Reverse Geocoding API 호출...")
            val response = naverReverseGeocodingService.reverseGeocode(coords = coords)
            Log.d("ReverseGeocodingRepository", "네이버 API 응답 수신 완료")
            Log.d("ReverseGeocodingRepository", "응답 데이터: $response")
            
            // 결과에서 주소 추출
            Log.d("ReverseGeocodingRepository", "주소 추출 시작...")
            val address = extractAddressFromResponse(response)
            Log.d("ReverseGeocodingRepository", "주소 추출 완료: $address")
            
            Log.d("ReverseGeocodingRepository", "=== Reverse Geocoding 성공 ===")
            address
            
        } catch (e: Exception) {
            Log.e("ReverseGeocodingRepository", "=== Reverse Geocoding 실패 ===")
            Log.e("ReverseGeocodingRepository", "에러 타입: ${e.javaClass.simpleName}")
            Log.e("ReverseGeocodingRepository", "에러 메시지: ${e.message}")
            Log.e("ReverseGeocodingRepository", "스택 트레이스: ${e.stackTraceToString()}")
            null
        }
    }
    
    private fun extractAddressFromResponse(response: NaverReverseGeocodeResponse): String? {
        return try {
            Log.d("ReverseGeocodingRepository", "=== 주소 추출 시작 ===")
            Log.d("ReverseGeocodingRepository", "응답 결과 개수: ${response.results?.size ?: 0}")
            
            val firstResult = response.results?.firstOrNull()
            if (firstResult == null) {
                Log.w("ReverseGeocodingRepository", "Reverse geocoding 결과가 없습니다")
                return null
            }
            
            Log.d("ReverseGeocodingRepository", "첫 번째 결과 분석...")
            val region = firstResult.region
            val land = firstResult.land
            
            Log.d("ReverseGeocodingRepository", "지역 정보: ${region.area1.name} ${region.area2.name} ${region.area3.name}")
            Log.d("ReverseGeocodingRepository", "토지 정보: ${land?.name ?: "없음"}")
            Log.d("ReverseGeocodingRepository", "건물번호1: ${land?.number1 ?: "없음"}")
            Log.d("ReverseGeocodingRepository", "건물번호2: ${land?.number2 ?: "없음"}")
            
            // 도로명 주소 우선, 없으면 지번 주소 사용
            val roadAddress = land?.name
            val buildingNumber = land?.number1?.let { num1 ->
                land.number2?.let { num2 -> "$num1-$num2" } ?: num1
            }
            
            Log.d("ReverseGeocodingRepository", "도로명 주소: $roadAddress")
            Log.d("ReverseGeocodingRepository", "건물번호: $buildingNumber")
            
            val address = if (!roadAddress.isNullOrBlank()) {
                // 도로명 주소: "도로명 + 건물번호"
                val result = "$roadAddress $buildingNumber"
                Log.d("ReverseGeocodingRepository", "도로명 주소 사용: $result")
                result
            } else {
                // 지번 주소: "시/도 + 시/군/구 + 읍/면/동"
                val result = "${region.area1.name} ${region.area2.name} ${region.area3.name}"
                Log.d("ReverseGeocodingRepository", "지번 주소 사용: $result")
                result
            }
            
            Log.d("ReverseGeocodingRepository", "=== 주소 추출 완료: $address ===")
            address
            
        } catch (e: Exception) {
            Log.e("ReverseGeocodingRepository", "=== 주소 추출 실패 ===")
            Log.e("ReverseGeocodingRepository", "에러 타입: ${e.javaClass.simpleName}")
            Log.e("ReverseGeocodingRepository", "에러 메시지: ${e.message}")
            Log.e("ReverseGeocodingRepository", "스택 트레이스: ${e.stackTraceToString()}")
            null
        }
    }
}


