package com.mskd.flux.features.catalog.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CatalogPreferences(
    val recentlyWatchedIds: List<Long>,
    val sortingMode: CatalogSortingMode,
    val viewMode: CatalogViewMode,
    val token: String,
)
