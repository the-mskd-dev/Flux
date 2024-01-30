package com.kaem.flux.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


data class LibraryPreferences(
    val sortOrder: SortOrder = SortOrder.RELEASE_DATE,
    val lastWatchedIds: List<Int> = listOf()
)

enum class SortOrder {
    NAME,
    RELEASE_DATE,
    ADDED_DATE
}

class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {

    private object PreferencesKeys {
        val SORT_ORDER = intPreferencesKey("sort_order")
        val LAST_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
    }

    val preferencesFlow: Flow<LibraryPreferences> = dataStore.data.map { preferences ->

        val sortOrderOrdinal = preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.RELEASE_DATE.ordinal

        val lastWatchedIdsString = preferences[PreferencesKeys.LAST_WATCHED_IDS] ?: "[]"
        val lastWatchedIds = gson.fromJson<List<Double>>(lastWatchedIdsString, List::class.java)

        LibraryPreferences(
            sortOrder = SortOrder.entries[sortOrderOrdinal],
            lastWatchedIds = lastWatchedIds.map { it.toInt() }
        )
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit {
            it[PreferencesKeys.SORT_ORDER] = sortOrder.ordinal
        }
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