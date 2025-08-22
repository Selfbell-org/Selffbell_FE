package com.selfbell.data.api.response

data class ContactListResponseDto(
    val items: List<ContactResponseDto>,
    val page: PageDto
)

data class PageDto(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)
