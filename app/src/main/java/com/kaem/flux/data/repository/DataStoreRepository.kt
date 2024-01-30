package com.kaem.flux.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


data class LibraryPreferences(
    val sortOrder: SortOrder = SortOrder.RELEASE_DATE
)

enum class SortOrder {
    NAME,
    RELEASE_DATE,
    ADDED_DATE
}

class DataStoreRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val SORT_ORDER = intPreferencesKey("sort_order")
    }

    val preferencesFlow: Flow<LibraryPreferences> = dataStore.data.map {
        val sortOrderOrdinal = it[PreferencesKeys.SORT_ORDER] ?: SortOrder.RELEASE_DATE.ordinal
        LibraryPreferences(
            sortOrder = SortOrder.entries[sortOrderOrdinal]
        )
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit {
            it[PreferencesKeys.SORT_ORDER] = sortOrder.ordinal
        }
    }

}