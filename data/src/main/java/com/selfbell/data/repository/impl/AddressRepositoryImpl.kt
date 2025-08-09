package com.selfbell.data.repository.impl

import com.selfbell.data.api.NaverApiService
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import retrofit2.http.Header
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val naverApiService: NaverApiService
) : AddressRepository {
    override suspend fun searchAddress(query: String): List<AddressModel> {
        val response = naverApiService.getGeocode(
            clientId = "X-NCP-APIGW-API-KEY-ID", // <-- Your real Client ID here
            clientSecret = "X-NCP-APIGW-API-KEY", // <-- Your real Client Secret here
            query = query
        )
        return response.addresses.map { it.toDomainModel() }
    }
}
//@Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
//@Header("X-NCP-APIGW-API-KEY") clientSecret: String,