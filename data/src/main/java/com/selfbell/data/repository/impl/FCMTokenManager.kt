package com.selfbell.data.repository.impl

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    private val securePreferences: SecurePreferences
) {

    companion object {
        private const val TAG = "FCMTokenManager"
        private const val FCM_TOKEN_KEY = "fcm_token_key"
    }

    /**
     * FCM 토큰을 가져옵니다.
     * @return FCM 토큰 문자열, 실패 시 null
     */
    suspend fun getFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM 토큰 가져오기 성공: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 가져오기 실패", e)
            null
        }
    }

    /**
     * ✅ FCM 토큰을 로컬에 저장하는 함수
     * @param token 저장할 FCM 토큰
     */
    suspend fun saveFCMToken(token: String) {
        securePreferences.saveString(FCM_TOKEN_KEY, token)
        Log.d(TAG, "FCM 토큰을 로컬에 저장했습니다.")
    }

    /**
     * FCM 토큰을 새로고침합니다.
     * @return 새로운 FCM 토큰 문자열, 실패 시 null
     */
    suspend fun refreshFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().deleteToken().await()
            val newToken = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM 토큰 새로고침 성공: $newToken")
            newToken
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 새로고침 실패", e)
            null
        }
    }
}