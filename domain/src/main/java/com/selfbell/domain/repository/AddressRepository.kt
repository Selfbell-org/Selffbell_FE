package com.selfbell.domain.repository

import com.selfbell.domain.model.AddressModel

interface AddressRepository {
    suspend fun searchAddress(query: String): List<AddressModel>
}