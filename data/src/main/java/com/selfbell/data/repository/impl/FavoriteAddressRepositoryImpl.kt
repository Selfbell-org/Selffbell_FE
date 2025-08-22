package com.selfbell.data.repository.impl

import com.selfbell.data.api.FavoriteAddressService
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.FavoriteAddress
import com.selfbell.domain.repository.FavoriteAddressRepository
import javax.inject.Inject

class FavoriteAddressRepositoryImpl @Inject constructor(
    private val FavoriteaddressService: FavoriteAddressService
) : FavoriteAddressRepository {
    override suspend fun getFavoriteAddresses(): List<FavoriteAddress> {
        return FavoriteaddressService.getFavoriteAddresses().items.map { it.toDomainModel() }
    }
}