package com.seasa.diary.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.fontDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_font")

class FontRepository(private val context: Context) {
    private object PreferencesKeys {
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_SIZE = floatPreferencesKey("font_size")
    }

    val fontFamily: Flow<String?> = context.fontDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY]
        }

    val fontSize: Flow<Float> = context.fontDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FONT_SIZE]?:18f
        }

    suspend fun saveFontState(fontType: String?, fontSize: Float) {
        context.fontDataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY] = fontType?:"default"
            preferences[PreferencesKeys.FONT_SIZE] = fontSize
        }
    }
}