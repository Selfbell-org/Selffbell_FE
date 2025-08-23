package com.selfbell.domain.repository

/**
 * Reverse Geocoding을 위한 Repository 인터페이스
 * 위도/경도 좌표를 주소로 변환합니다.
 */
interface ReverseGeocodingRepository {
    /**
     * 위도/경도 좌표를 주소로 변환합니다.
     * 
     * @param lat 위도
     * @param lon 경도
     * @return 주소 문자열, 실패 시 null
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): String?
}


