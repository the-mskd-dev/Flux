package com.mskd.flux.features.catalog.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CatalogDataStoreImpl(val catalogDataStore: DataStore<Preferences>) : CatalogDataStore {

    object Keys {
        val SORTING_MODE = intPreferencesKey("sorting_mode")
        val VIEW_MODE = intPreferencesKey("view_mode")
        // TODO: Remove with the Play Store announcement by March 2027
        val PLAY_STORE_PROMO_SEEN = booleanPreferencesKey("play_store_promo_seen")
    }

    override val flow: Flow<CatalogDataStore.State> = catalogDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val sortingOption = preferences[Keys.SORTING_MODE]?.let { CatalogSortingMode.fromOrdinal(it) } ?: CatalogSortingMode.LAST_MODIFICATION
            val viewOption = preferences[Keys.VIEW_MODE]?.let { CatalogViewMode.fromOrdinal(it) } ?: CatalogViewMode.BY_TYPE

            CatalogDataStore.State(
                sortingMode = sortingOption,
                viewMode = viewOption,
                playStorePromoSeen = preferences[Keys.PLAY_STORE_PROMO_SEEN] ?: false,
            )
        }

    override suspend fun setSortingMode(mode: CatalogSortingMode) {
        catalogDataStore.edit { preferences ->
            preferences[Keys.SORTING_MODE] = mode.ordinal
        }
    }

    override suspend fun setViewMode(mode: CatalogViewMode) {
        catalogDataStore.edit { preferences ->
            preferences[Keys.VIEW_MODE] = mode.ordinal
        }
    }

    // TODO: Remove with the Play Store announcement by March 2027
    override suspend fun setPlayStorePromoSeen() {
        catalogDataStore.edit { preferences ->
            preferences[Keys.PLAY_STORE_PROMO_SEEN] = true
        }
    }

}