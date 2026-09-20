package com.mskd.flux.features.privateFolder.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class PrivateFolderDataStoreImpl(val privateFolderDataStore: DataStore<Preferences>) : PrivateFolderDataStore {

    object Keys {
        val PRIVATE_FOLDER_ENABLED = booleanPreferencesKey("private_folder_enabled")
        val PRIVATE_FOLDER_INCLUDE_NSFW = booleanPreferencesKey("private_folder_include_nsfw")
        val PRIVATE_FOLDER_PIN = stringPreferencesKey("private_folder_pin")
    }

    private companion object {
        const val PIN_SALT = "flux.private_folder"
    }

    override val flow: Flow<PrivateFolderDataStore.State> = privateFolderDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val enabled = preferences[Keys.PRIVATE_FOLDER_ENABLED] ?: false
            val includeNsfw = preferences[Keys.PRIVATE_FOLDER_INCLUDE_NSFW] ?: true

            PrivateFolderDataStore.State(
                enabled = enabled,
                includeNsfw = includeNsfw
            )
        }

    override suspend fun setEnabled(enabled: Boolean) {
        privateFolderDataStore.edit { preferences ->
            preferences[Keys.PRIVATE_FOLDER_ENABLED] = enabled

            if (!enabled)
                preferences.remove(Keys.PRIVATE_FOLDER_PIN)
        }
    }

    override suspend fun setIncludeNsfw(includeNsfw: Boolean) {
        privateFolderDataStore.edit { preferences ->
            preferences[Keys.PRIVATE_FOLDER_INCLUDE_NSFW] = includeNsfw
        }
    }

    override suspend fun setPin(pin: String) {
        privateFolderDataStore.edit { preferences ->
            preferences[Keys.PRIVATE_FOLDER_PIN] = hash(pin = pin)
        }
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val storedPin = privateFolderDataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
            .firstOrNull()?.get(Keys.PRIVATE_FOLDER_PIN)

        return storedPin != null && storedPin == hash(pin = pin)
    }

    private fun hash(pin: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest("$PIN_SALT$pin".toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

}
