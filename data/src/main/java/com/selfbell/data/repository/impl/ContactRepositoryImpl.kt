package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.request.ContactRequestDto
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.repository.ContactRepository
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactService: ContactService // 📌 Hilt를 통해 ContactService 주입
) : ContactRepository {

    // 이전에 구현한 AuthRepository의 함수들은 여기에 포함되지 않습니다.
    // AuthRepositoryImpl과 별도의 파일로 관리됩니다.

    override suspend fun getContactsFromServer(token: String, status: String, page: Int, size: Int): List<ContactRelationship> {
        return try {
            val response = contactService.getContacts(
                token = "Bearer $token",
                status = status,
                page = page,
                size = size
            )
            Log.d("ContactRepo", "서버 연락처 목록 로드 성공. Total: ${response.page.totalElements}")
            // 서버 응답 DTO를 도메인 모델로 변환하는 로직이 필요합니다.
            // 여기서는 임시로 비어있는 목록을 반환합니다.
            emptyList()
        } catch (e: Exception) {
            Log.e("ContactRepo", "서버 연락처 목록 로드 실패", e)
            throw e
        }
    }

    override suspend fun sendContactRequest(token: String, toPhoneNumber: String) {
        val request = ContactRequestDto(toPhoneNumber)
        try {
            val response = contactService.sendContactRequest(
                token = "Bearer $token",
                request = request
            )
            Log.d("ContactRepo", "보호자 요청 전송 성공: toPhoneNumber=$toPhoneNumber, status=${response.status}")
        } catch (e: Exception) {
            Log.e("ContactRepo", "보호자 요청 전송 실패", e)
            throw e
        }
    }

    override suspend fun acceptContactRequest(token: String, contactId: Long) {
        try {
            val response = contactService.acceptContactRequest(
                token = "Bearer $token",
                contactId = contactId
            )
            Log.d("ContactRepo", "보호자 요청 수락 성공: contactId=$contactId, status=${response.status}")
        } catch (e: Exception) {
            Log.e("ContactRepo", "보호자 요청 수락 실패", e)
            throw e
        }
    }
}