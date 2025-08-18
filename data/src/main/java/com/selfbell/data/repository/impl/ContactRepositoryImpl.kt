package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.request.ContactRequestDto
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.repository.ContactRepository
import javax.inject.Inject
import kotlinx.coroutines.delay // 딜레이를 추가하여 네트워크 지연을 모방합니다.

class ContactRepositoryImpl @Inject constructor(
    // 실제 API 연동 시 Hilt를 통해 ContactService를 주입받습니다.
    private val contactService: ContactService
) : ContactRepository {

    // 이전에 구현한 AuthRepository의 함수들은 여기에 포함되지 않습니다.
    // AuthRepositoryImpl과 별도의 파일로 관리됩니다.

    override suspend fun getContactsFromServer(token: String, status: String, page: Int, size: Int): List<ContactRelationship> {
        // [START] Mocking - 백엔드 연동 전까지 임시로 사용하는 코드입니다.
        // 실제 API 연동 시, 아래 Mocking 코드를 제거하고,
        // 주석 처리된 try-catch 블록의 API 호출 코드를 활성화하세요.
        delay(500) // 로딩 상태를 보여주기 위해 0.5초 딜레이 추가
        Log.d("ContactRepo", "Mocking: getContactsFromServer 함수 호출 (status=$status)")

        return when (status) {
            "PENDING" -> listOf(
                ContactRelationship(
                    contactId = 3L,
                    other = ContactUser(phoneNumber = "010-1111-2222", name = "요청받은 친구"),
                    status = "PENDING",
                    sharePermission = false
                )
            )
            "ACCEPTED" -> listOf(
                ContactRelationship(
                    contactId = 1L,
                    other = ContactUser(phoneNumber = "010-1234-5678", name = "엄마"),
                    status = "ACCEPTED",
                    sharePermission = true
                ),
                ContactRelationship(
                    contactId = 2L,
                    other = ContactUser(phoneNumber = "010-9876-5432", name = "아빠"),
                    status = "ACCEPTED",
                    sharePermission = true
                )
            )
            else -> emptyList()
        }
        // [END] Mocking

        /* [START] 실제 API 연동 코드 - 백엔드 개발 완료 후 이 부분을 활성화하세요.
        return try {
            val response = contactService.getContacts(
                token = "Bearer $token",
                status = status,
                page = page,
                size = size
            )
            Log.d("ContactRepo", "서버 연락처 목록 로드 성공. Total: ${response.page.totalElements}")
            // TODO: 서버 응답 DTO를 도메인 모델로 변환하는 로직을 여기에 구현해야 합니다.
            // 예: response.items.map { it.toDomainModel() }
            emptyList()
        } catch (e: Exception) {
            Log.e("ContactRepo", "서버 연락처 목록 로드 실패", e)
            throw e
        }
        [END] 실제 API 연동 코드 */
    }

    override suspend fun sendContactRequest(token: String, toPhoneNumber: String) {
        // [START] Mocking - 백엔드 연동 전까지 임시로 사용하는 코드입니다.
        // 실제 API 연동 시, 아래 Mocking 코드를 제거하고
        // 주석 처리된 try-catch 블록의 API 호출 코드를 활성화하세요.
        delay(500)
        Log.d("ContactRepo", "Mocking: sendContactRequest 함수 호출 (toPhoneNumber=$toPhoneNumber)")

        // 특정 전화번호에 대해 에러를 발생시키는 Mocking
        if (toPhoneNumber == "010-9999-9999") {
            Log.e("ContactRepo", "Mocking: 409 Conflict - 이미 존재하는 사용자입니다.")
            throw IllegalStateException("이미 존재하는 사용자입니다.")
        } else if (toPhoneNumber == "010-8888-8888") {
            Log.e("ContactRepo", "Mocking: 404 Not Found - 가입되지 않은 사용자입니다.")
            throw NoSuchElementException("가입되지 않은 사용자입니다.")
        }
        // [END] Mocking

        /* [START] 실제 API 연동 코드 - 백엔드 개발 완료 후 이 부분을 활성화하세요.
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
        [END] 실제 API 연동 코드 */
    }

    override suspend fun acceptContactRequest(token: String, contactId: Long) {
        // [START] Mocking - 백엔드 연동 전까지 임시로 사용하는 코드입니다.
        // 실제 API 연동 시, 아래 Mocking 코드를 제거하고
        // 주석 처리된 try-catch 블록의 API 호출 코드를 활성화하세요.
        delay(500)
        Log.d("ContactRepo", "Mocking: acceptContactRequest 함수 호출 (contactId=$contactId)")

        // 특정 ID에 대해 에러를 발생시키는 Mocking
        if (contactId == 99L) {
            Log.e("ContactRepo", "Mocking: 409 Conflict - 이미 수락된 요청입니다.")
            throw IllegalStateException("이미 수락된 요청입니다.")
        }
        // [END] Mocking

        /* [START] 실제 API 연동 코드 - 백엔드 개발 완료 후 이 부분을 활성화하세요.
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
        [END] 실제 API 연동 코드 */
    }
}