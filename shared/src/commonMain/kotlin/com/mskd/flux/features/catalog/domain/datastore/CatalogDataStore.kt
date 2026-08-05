package com.mskd.flux.features.catalog.domain.datastore

import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import kotlinx.coroutines.flow.Flow

interface CatalogDataStore {

    val flow: Flow<State>

    suspend fun setSortingMode(mode: CatalogSortingMode)
    suspend fun setViewMode(mode: CatalogViewMode)

    data class State(
        val sortingMode: CatalogSortingMode = CatalogSortingMode.LAST_MODIFICATION,
        val viewMode: CatalogViewMode = CatalogViewMode.BY_TYPE
    )

}