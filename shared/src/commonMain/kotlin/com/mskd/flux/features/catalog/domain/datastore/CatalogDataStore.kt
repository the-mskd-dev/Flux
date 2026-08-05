package com.mskd.flux.features.catalog.domain.datastore

import com.mskd.flux.features.catalog.domain.model.CatalogSortingOption
import kotlinx.coroutines.flow.Flow

interface CatalogDataStore {

    val flow: Flow<State>

    suspend fun setSortingOption(option: CatalogSortingOption)

    data class State(
        val sortingOption: CatalogSortingOption = CatalogSortingOption.LAST_MODIFICATION
    )

}