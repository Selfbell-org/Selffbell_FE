package com.selfbell.data.api

import com.selfbell.data.api.response.FavoriteAddressListResponse
import retrofit2.http.GET

interface FavoriteAddressService {
    @GET("api/v1/addresses")
    suspend fun getFavoriteAddresses(): FavoriteAddressListResponse
}