package com.kaem.flux.data.repository.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

class UserRepositoryImpl(
    val userDataStore: DataStore<Preferences>,
    private val gson: Gson
) : UserRepository {

    object Keys {
        val RECENTYL_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

        val WATCHED_MESSAGES_IDS = stringPreferencesKey("watched_messages_ids")

    }

    override val flow: Flow<UserPreferences> = userDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val watchedIdsString = preferences[Keys.RECENTYL_WATCHED_IDS] ?: "[]"
            val watchedIds = gson.fromJson<List<Double>>(watchedIdsString, List::class.java).map { it.toLong() }
            val syncTime = preferences[Keys.LAST_SYNC_TIME] ?: 0L
            val watchedMessagesIdsString = preferences[Keys.WATCHED_MESSAGES_IDS] ?: "[]"
            val watchedMessagesIds = gson.fromJson<List<Double>>(watchedMessagesIdsString, List::class.java).map { it.toInt() }

            UserPreferences(
                recentlyWatchedIds = watchedIds,
                syncTime = syncTime,
                watchedMessagesIds = watchedMessagesIds
            )
        }

    override suspend fun addToRecentlyWatched(artworkId: Long) {
        userDataStore.edit { preferences ->
            val lastWatchedIds = ArrayList(flow.first().recentlyWatchedIds)

            if (lastWatchedIds.none { it == artworkId }) {

                lastWatchedIds.add(0, artworkId)

                preferences[Keys.RECENTYL_WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))
            }

        }
    }

    override suspend fun removeFromRecentlyWatched(artworkId: Long) {
        userDataStore.edit { preferences ->
            val lastWatchedIds = ArrayList(flow.first().recentlyWatchedIds)
            lastWatchedIds.remove(artworkId)
            preferences[Keys.RECENTYL_WATCHED_IDS] = gson.toJson(lastWatchedIds)

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

    override suspend fun setMessageAsWatched(messageId: Int) {
        userDataStore.edit { preferences ->
            val watchedMessagesIds = flow.first().watchedMessagesIds
            preferences[Keys.WATCHED_MESSAGES_IDS] = gson.toJson(watchedMessagesIds + messageId)
        }
    }

}