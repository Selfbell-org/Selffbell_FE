package com.selfbell.data.mapper

import com.selfbell.data.api.response.AddressResponse
import com.selfbell.domain.model.AddressModel

fun AddressResponse.toDomainModel(): AddressModel {
    return AddressModel(
        roadAddress = this.roadAddress,
        jibunAddress = this.jibunAddress,
        x = this.x,
        y = this.y
    )
}