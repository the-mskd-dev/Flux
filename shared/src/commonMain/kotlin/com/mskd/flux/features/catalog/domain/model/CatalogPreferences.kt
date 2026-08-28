package com.mskd.flux.features.catalog.domain.model

import androidx.compose.runtime.Immutable
import com.mskd.flux.features.history.domain.model.HistoryEntry

@Immutable
data class CatalogPreferences(
    val history: List<HistoryEntry> = emptyList(),
    val sortingMode: CatalogSortingMode,
    val viewMode: CatalogViewMode,
    val token: String,
)
