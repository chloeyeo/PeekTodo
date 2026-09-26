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

    /** Blur mode: show only the task count on the lock screen. Defaults to off. */
    val blurMode: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.BLUR_MODE] ?: false }

    /** Pin notification: keep it ongoing and re-post it if the user dismisses it. Defaults to off. */
    val pinNotification: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.PIN_NOTIFICATION] ?: false }

    suspend fun setBlurMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.BLUR_MODE] = enabled }
    }

    suspend fun setPinNotification(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.PIN_NOTIFICATION] = enabled }
    }

    private object Keys {
        val BLUR_MODE = booleanPreferencesKey("blur_mode")
        val PIN_NOTIFICATION = booleanPreferencesKey("pin_notification")
    }
}
