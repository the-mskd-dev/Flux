package com.mskd.flux.features.catalog.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CatalogPreferences(
    val recentlyWatchedIds: List<Long>,
    val sortingOption: CatalogSortingMode,
    val token: String,
)
