package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore

data class CatalogPreferences(
    val recentlyWatchedIds: List<Long>,
    val sortingOption: CatalogSortingOption,
    val token: String,
)
