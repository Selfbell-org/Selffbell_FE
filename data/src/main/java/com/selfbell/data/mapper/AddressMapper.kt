package com.selfbell.data.mapper

import com.selfbell.data.api.response.AddressResponse
import com.selfbell.domain.model.AddressModel

fun AddressResponse.toDomainModel(): AddressModel {
    return AddressModel(
        roadAddress = this.roadAddress ?: "",
        jibunAddress = this.jibunAddress ?: "",
        x = this.x ?: "0.0", // String?을 Double로 변환, null이거나 변환 실패 시 0.0
        y = this.y ?: "0.0"
    )
}