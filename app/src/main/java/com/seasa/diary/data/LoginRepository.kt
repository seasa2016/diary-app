package com.seasa.diary.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// 為 DataStore 創建一個單例實例
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class LoginRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val ID_TOKEN = stringPreferencesKey("id_token")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    val userDisplayName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_DISPLAY_NAME]
        }

    val idToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ID_TOKEN]
        }

    suspend fun saveLoginState(isLoggedIn: Boolean, userId: String?, displayName: String?, idToken: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.USER_ID] = userId ?: "" // 如果為 null 存儲空字串
            preferences[PreferencesKeys.USER_DISPLAY_NAME] = displayName ?: ""
            preferences[PreferencesKeys.ID_TOKEN] = idToken ?: ""
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // 檢查是否有儲存的登入狀態
    suspend fun checkSavedLoginState(): Boolean {
        return isLoggedIn.firstOrNull() ?: false
    }
}