// ContactRepositoryImpl.kt
package com.selfbell.data.repository.impl

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.request.ContactRequestRequest
import com.selfbell.data.mapper.toDomain
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactRelationshipStatus
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.repository.ContactRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepositoryImpl @Inject constructor(
    private val contactService: ContactService,
    private val context: Context
) : ContactRepository {

    override suspend fun getDeviceContacts(): List<ContactUser> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactUser>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID) // ✅ ID 인덱스 추가
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex) ?: "" // ✅ ID 값 가져오기
                val name = it.getString(nameIndex) ?: ""
                val number = it.getString(numberIndex)
                    .replace("-", "")
                    .replace(" ", "")

                if (number.isNotBlank() && contacts.none { contact -> contact.phoneNumber == number }) {
                    // ✅ id와 relationshipStatus 필드를 포함하여 ContactUser 생성
                    contacts.add(ContactUser(id = id, name = name, phoneNumber = number, isExists = false, relationshipStatus = ContactRelationshipStatus.NONE))
                }
            }
        }
        Log.d("ContactRepository", "로컬 연락처 로드 완료: ${contacts.size}개")
        contacts.distinctBy { it.phoneNumber }
    }

    override suspend fun checkUserExists(phoneNumber: String): Boolean {
        return try {
            val response = contactService.checkUserExists(phoneNumber)
            Log.d("ContactRepository", "사용자 존재 여부 확인 성공: $phoneNumber -> ${response.isExists}")
            response.isExists
        } catch (e: Exception) {
            Log.e("ContactRepository", "사용자 존재 여부 확인 실패: $phoneNumber", e)
            false
        }
    }

    override suspend fun sendContactRequest(toPhoneNumber: String) {
        val request = ContactRequestRequest(toPhoneNumber)
        try {
            val response = contactService.sendContactRequest(request)
            if (response.isSuccessful) {
                Log.d("ContactRepository", "보호자 요청 전송 성공: $toPhoneNumber")
            } else {
                when (response.code()) {
                    404 -> throw IllegalStateException("가입되지 않은 사용자입니다.")
                    409 -> throw IllegalStateException("이미 관계가 존재합니다.")
                    else -> throw IllegalStateException("보호자 요청 전송 실패: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("ContactRepository", "보호자 요청 전송 실패", e)
            throw e
        }
    }

    override suspend fun getContactsFromServer(status: String, page: Int, size: Int): List<ContactRelationship> {
        return try {
            val response = contactService.getContacts(status, page, size)
            response.items.map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            Log.e("ContactRepo", "서버 연락처 목록 로드 실패", e)
            throw e
        }
    }

    override suspend fun acceptContactRequest(contactId: Long) {
        try {
            val response = contactService.acceptContactRequest(contactId)
            Log.d("ContactRepo", "보호자 요청 수락 성공: contactId=$contactId")
        } catch (e: Exception) {
            Log.e("ContactRepo", "보호자 요청 수락 실패", e)
            throw e
        }
    }

    override suspend fun getUserFCMToken(userId: String): String? {
        return try {
            val response = contactService.getUserFCMToken(userId)
            if (response.success && response.fcmToken != null) {
                Log.d("ContactRepo", "FCM 토큰 가져오기 성공: userId=$userId")
                response.fcmToken
            } else {
                Log.d("ContactRepo", "FCM 토큰이 없음: userId=$userId")
                null
            }
        } catch (e: Exception) {
            Log.e("ContactRepo", "FCM 토큰 가져오기 실패: userId=$userId", e)
            null
        }
    }
}