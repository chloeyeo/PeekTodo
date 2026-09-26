package com.chloeyeo.peektodo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    /** Blur mode: mask to-do text on the lock screen and reveal on unlock. Defaults to off. */
    val blurMode: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.BLUR_MODE] ?: false }

    suspend fun setBlurMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.BLUR_MODE] = enabled }
    }

    private object Keys {
        val BLUR_MODE = booleanPreferencesKey("blur_mode")
    }
}
