package com.selfbell.data.repository.impl

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.selfbell.data.api.ContactService
import com.selfbell.data.api.request.ContactRequestRequest
import com.selfbell.data.api.response.ContactResponseDto
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactRelationshipStatus
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.repository.ContactRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

class ContactRepositoryImpl @Inject constructor(
    private val contactService: ContactService,
    private val context: Context
) : ContactRepository {

    override suspend fun loadDeviceContactsWithUserCheck(): List<ContactUser> {
        val deviceContacts = loadDeviceContacts()
        val contactsWithUserCheck = mutableListOf<ContactUser>()
        
        deviceContacts.forEach { contact ->
            try {
                val isExists = checkUserExists(contact.phoneNumber)
                contactsWithUserCheck.add(
                    ContactUser(
                        id = contact.id,
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        isExists = isExists
                    )
                )
                // API 호출 간격을 두어 서버 부하 방지
                delay(100)
            } catch (e: Exception) {
                Log.e("ContactRepo", "사용자 존재 확인 실패: ${contact.phoneNumber}", e)
                // 에러가 발생해도 연락처는 추가하되 존재 여부는 false로 설정
                contactsWithUserCheck.add(
                    ContactUser(
                        id = contact.id,
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        isExists = false
                    )
                )
            }
        }
        
        return contactsWithUserCheck
    }

    override suspend fun loadDeviceContactsOnly(): List<ContactUser> {
        // 서버 체크 없이 즉시 로컬 연락처 반환
        return loadDeviceContacts()
    }

    override suspend fun checkUserExists(phoneNumber: String): Boolean {
        return try {
            val response = contactService.checkUserExists(phoneNumber)
            response.isExists
        } catch (e: Exception) {
            Log.e("ContactRepo", "사용자 존재 확인 실패: $phoneNumber", e)
            false
        }
    }

    override suspend fun getContactsFromServer(status: String, page: Int, size: Int): List<ContactRelationship> {
        return try {
            val response = contactService.getContacts(status, page, size)
            response.items.map { dto -> dto.toDomainFallback() }
        } catch (e: Exception) {
            Log.e("ContactRepo", "서버 연락처 목록 로드 실패", e)
            throw e
        }
    }

    override suspend fun sendContactRequest(toPhoneNumber: String) {
        val request = ContactRequestRequest(toPhoneNumber)
        try {
            val response = contactService.sendContactRequest(request)
            if (response.isSuccessful) {
                Log.d("ContactRepo", "보호자 요청 전송 성공: $toPhoneNumber")
            } else {
                when (response.code()) {
                    404 -> throw IllegalStateException("가입되지 않은 사용자입니다.")
                    409 -> throw IllegalStateException("이미 관계가 존재합니다.")
                    else -> throw IllegalStateException("보호자 요청 전송 실패: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("ContactRepo", "보호자 요청 전송 실패", e)
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

    private fun loadDeviceContacts(): List<ContactUser> {
        val contacts = mutableListOf<ContactUser>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        
        val selection = "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = ?"
        val selectionArgs = arrayOf("1")
        
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                
                // 전화번호 가져오기
                val phoneNumbers = getPhoneNumbers(id)
                phoneNumbers.forEach { phoneNumber ->
                    contacts.add(
                        ContactUser(
                            id = id,
                            name = name,
                            phoneNumber = phoneNumber
                        )
                    )
                }
            }
        }
        
        return contacts
    }

    private fun getPhoneNumbers(contactId: String): List<String> {
        val phoneNumbers = mutableListOf<String>()
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)
        
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                // 전화번호 정규화 (하이픈 제거)
                val normalizedPhone = phoneNumber.replace("-", "")
                phoneNumbers.add(normalizedPhone)
            }
        }
        
        return phoneNumbers
    }
}

private fun ContactResponseDto.toDomainFallback(): ContactRelationship {
    val statusEnum = when (status.uppercase()) {
        "PENDING" -> ContactRelationshipStatus.PENDING
        "ACCEPTED" -> ContactRelationshipStatus.ACCEPTED
        "REJECTED" -> ContactRelationshipStatus.REJECTED
        else -> ContactRelationshipStatus.NONE
    }
    return ContactRelationship(
        id = contactId.toString(),
        fromUserId = "",
        toUserId = "",
        fromPhoneNumber = "", // 서버 응답에 송신자 번호가 없으므로 비워둠
        toPhoneNumber = other.phoneNumber,
        status = statusEnum,
        createdAt = "",
        updatedAt = "",
        sharePermission = sharePermission
    )
}