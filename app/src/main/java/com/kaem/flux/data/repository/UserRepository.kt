package com.kaem.flux.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException

val Context.userDataStore by preferencesDataStore(
    name ="UserDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

data class UserPreferences(
    val watchedIds: List<Long> = listOf(),
    val syncTime: Long = 0L
)

class UserRepository(
    val userDataStore: DataStore<Preferences>,
    private val gson: Gson
) {

    object Keys {
        val WATCHED_IDS = stringPreferencesKey("last_watched_ids")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val flow: Flow<UserPreferences> = userDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val watchedIdsString = preferences[Keys.WATCHED_IDS] ?: "[]"
            val watchedIds = gson.fromJson<List<Double>>(watchedIdsString, List::class.java).map { it.toLong() }
            val syncTime = preferences[Keys.LAST_SYNC_TIME] ?: 0L

            UserPreferences(
                watchedIds = watchedIds,
                syncTime = syncTime
            )
        }

    suspend fun addWatchedMedia(id: Long) {
        userDataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[Keys.WATCHED_IDS] ?: "[]"
            val lastWatchedIds: ArrayList<Long> = gson.fromJson<ArrayList<Long>>(lastWatchedIdsString, ArrayList::class.java)

            if (lastWatchedIds.none { it == id }) {

                lastWatchedIds.add(0, id)

                preferences[Keys.WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))
            }

        }
    }

    suspend fun removeWatchedMedia(id: Long) {
        userDataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[Keys.WATCHED_IDS] ?: "[]"
            val type = object : TypeToken<ArrayList<Long>>() {}.type
            val lastWatchedIds: ArrayList<Long> = gson.fromJson(lastWatchedIdsString, type)

            lastWatchedIds.remove(id)
            preferences[Keys.WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))

        }
    }

    suspend fun setSyncTime(syncTime: Long) {
        userDataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = syncTime
        }
    }

}