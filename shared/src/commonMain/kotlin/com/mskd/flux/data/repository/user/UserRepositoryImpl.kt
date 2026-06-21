package com.mskd.flux.data.repository.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okio.IOException

class UserRepositoryImpl(
    val userDataStore: DataStore<Preferences>,
    private val json: Json
) : UserRepository {

    object Keys {
        val RECENTLY_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val CURRENT_VERSION_CODE = intPreferencesKey("version_code")

        val WATCHED_MESSAGES_IDS = stringPreferencesKey("watched_messages_ids")
        val PIP_IS_ENABLED = booleanPreferencesKey("pip_is_enabled")

    }

    override val flow: Flow<UserRepository.State> = userDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val watchedIdsString = preferences[Keys.RECENTLY_WATCHED_IDS] ?: "[]"
            val watchedIds = json.decodeFromString<List<Long>>(watchedIdsString)
            val syncTime = preferences[Keys.LAST_SYNC_TIME] ?: 0L
            val watchedMessagesIdsString = preferences[Keys.WATCHED_MESSAGES_IDS] ?: "[]"
            val watchedMessagesIds = json.decodeFromString<List<Int>>(watchedMessagesIdsString)
            val versionCode = preferences[Keys.CURRENT_VERSION_CODE] ?: -1
            val pipIsEnabled = preferences[Keys.PIP_IS_ENABLED] ?: true

            UserRepository.State(
                recentlyWatchedIds = watchedIds,
                syncTime = syncTime,
                watchedMessagesIds = watchedMessagesIds,
                versionCode = versionCode,
                pipIsEnabled = pipIsEnabled
            )
        }

    override suspend fun addToRecentlyWatched(artworkId: Long) {
        userDataStore.edit { preferences ->
            val lastWatchedIds = ArrayList(flow.first().recentlyWatchedIds)

            // Place recently watch in first position
            lastWatchedIds.remove(artworkId)
            lastWatchedIds.add(0, artworkId)

            preferences[Keys.RECENTLY_WATCHED_IDS] = json.encodeToString(lastWatchedIds.take(4))

        }
    }

    override suspend fun removeFromRecentlyWatched(artworkId: Long) {
        userDataStore.edit { preferences ->
            val lastWatchedIds = ArrayList(flow.first().recentlyWatchedIds)
            lastWatchedIds.remove(artworkId)
            preferences[Keys.RECENTLY_WATCHED_IDS] = json.encodeToString(lastWatchedIds)

        }
    }

    override suspend fun setSyncTime(syncTime: Long) {
        userDataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = syncTime
        }
    }

    override suspend fun getSyncTime() : Long {
        return flow.first().syncTime
    }

    override suspend fun setVersionCode(versionCode: Int) {
        userDataStore.edit { preferences ->
            preferences[Keys.CURRENT_VERSION_CODE] = versionCode
        }
    }

    override suspend fun getVersionCode(): Int {
        return flow.first().versionCode
    }

    override suspend fun setMessageAsWatched(messageId: Int) {
        userDataStore.edit { preferences ->
            val watchedMessagesIds = flow.first().watchedMessagesIds
            preferences[Keys.WATCHED_MESSAGES_IDS] = json.encodeToString(watchedMessagesIds + messageId)
        }
    }

    override suspend fun enablePip(enable: Boolean) {
        userDataStore.edit { preferences ->
            preferences[Keys.PIP_IS_ENABLED] = enable
        }
    }

}