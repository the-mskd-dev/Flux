package com.kaem.flux.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


data class LibraryPreferences(
    val lastWatchedIds: List<Int> = listOf()
)


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {

    private object PreferencesKeys {
        val LAST_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
    }

    val preferencesFlow: Flow<LibraryPreferences> = dataStore.data.map { preferences ->

        val lastWatchedIdsString = preferences[PreferencesKeys.LAST_WATCHED_IDS] ?: "[]"
        val lastWatchedIds = gson.fromJson<List<Double>>(lastWatchedIdsString, List::class.java)

        LibraryPreferences(
            lastWatchedIds = lastWatchedIds.map { it.toInt() }
        )
    }

    suspend fun addWatchedArtwork(id: Int) {
        dataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[PreferencesKeys.LAST_WATCHED_IDS] ?: "[]"
            val lastWatchedIds: ArrayList<Int> = gson.fromJson<ArrayList<Int>>(lastWatchedIdsString, ArrayList::class.java)

            if (lastWatchedIds.none { it == id }) {

                lastWatchedIds.add(0, id)

                if (lastWatchedIds.size > 4)
                    lastWatchedIds.removeLast()

                preferences[PreferencesKeys.LAST_WATCHED_IDS] = gson.toJson(lastWatchedIds)
            }

        }
    }

}