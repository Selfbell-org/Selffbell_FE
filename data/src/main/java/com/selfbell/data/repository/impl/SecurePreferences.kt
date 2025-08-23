package com.selfbell.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

@Singleton
class SecurePreferences @Inject constructor(
    private val context: Context
) : TokenManager {

    private val dataStore = context.dataStore

    override suspend fun saveAccessToken(token: String) {
        val cleaned = token.trim()
        if (cleaned.isEmpty()) return
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = cleaned
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        val cleaned = token.trim()
        if (cleaned.isEmpty()) return
        dataStore.edit { preferences ->
            preferences[KEY_REFRESH_TOKEN] = cleaned
        }
    }

    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_ACCESS_TOKEN]?.trim()
        }.first()
    }

    override suspend fun getRefreshToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_REFRESH_TOKEN]?.trim()
        }.first()
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
        }
    }

    override suspend fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    // ✅ String 값을 저장하는 함수 추가
    suspend fun saveString(key: String, value: String) {
        val stringKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[stringKey] = value
        }
    }

    // ✅ String 값을 가져오는 함수 추가
    suspend fun getString(key: String): String? {
        val stringKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[stringKey]
        }.first()
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        // ✅ FCM 토큰을 위한 키 추가 (FCMTokenManager에서 사용)
        const val KEY_FCM_TOKEN = "fcm_token_key"
    }
}