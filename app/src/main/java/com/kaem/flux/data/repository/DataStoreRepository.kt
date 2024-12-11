package com.kaem.flux.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.internal.toLongOrDefault
import javax.inject.Inject


data class LibraryPreferences(
    val lastWatchedIds: List<Long> = listOf(),
    val lastSyncTime: Long = Long.MIN_VALUE
)


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {

    private object PreferencesKeys {
        val LAST_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    }

    val preferencesFlow: Flow<LibraryPreferences> = dataStore.data.map { preferences ->

        val lastWatchedIdsString = preferences[PreferencesKeys.LAST_WATCHED_IDS] ?: "[]"
        val lastWatchedIds = gson.fromJson<List<Double>>(lastWatchedIdsString, List::class.java)
        val lastSyncTime = (preferences[PreferencesKeys.LAST_SYNC_TIME] ?: "0").toLongOrDefault(0)

        LibraryPreferences(
            lastWatchedIds = lastWatchedIds.map { it.toLong() },
            lastSyncTime = lastSyncTime
        )
    }

    suspend fun addWatchedArtwork(id: Long) {
        dataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[PreferencesKeys.LAST_WATCHED_IDS] ?: "[]"
            val lastWatchedIds: ArrayList<Long> = gson.fromJson<ArrayList<Long>>(lastWatchedIdsString, ArrayList::class.java)

            if (lastWatchedIds.none { it == id }) {

                lastWatchedIds.add(0, id)

                preferences[PreferencesKeys.LAST_WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))
            }

        }
    }

    suspend fun saveSyncTime(syncTime: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = syncTime.toString()
        }
    }

}