package com.selfbell.data.mapper

import com.selfbell.data.api.response.FavoriteAddressItem
import com.selfbell.domain.model.FavoriteAddress

fun FavoriteAddressItem.toDomainModel(): FavoriteAddress {
    return FavoriteAddress(
        name = this.name,
        address = this.address,
        lat = this.lat,
        lon = this.lon
    )
}