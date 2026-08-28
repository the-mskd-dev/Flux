package com.mskd.flux.features.catalog.domain.model


import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CatalogPreferences(
    val history: ImmutableList<HistoryEntry> = persistentListOf(),
    val sortingMode: CatalogSortingMode,
    val viewMode: CatalogViewMode,
    val token: String,
)
