package com.selfbell.domain.repository

import com.selfbell.domain.model.FavoriteAddress

interface FavoriteAddressRepository {
    suspend fun getFavoriteAddresses(): List<FavoriteAddress>
}