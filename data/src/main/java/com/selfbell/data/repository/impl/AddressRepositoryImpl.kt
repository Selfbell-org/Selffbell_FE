package com.selfbell.data.repository.impl

import com.selfbell.data.api.NaverApiService
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val naverApiService: NaverApiService
) : AddressRepository {
    override suspend fun searchAddress(query: String): List<AddressModel> {
        val response = naverApiService.getGeocode(
            clientId = "YOUR_CLIENT_ID", // TODO: 실제 키로 교체
            clientSecret = "YOUR_CLIENT_SECRET", // TODO: 실제 키로 교체
            query = query
        )
        return response.addresses.map { it.toDomainModel() }
    }
}