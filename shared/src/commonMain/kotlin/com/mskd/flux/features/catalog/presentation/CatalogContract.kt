package com.mskd.flux.features.catalog.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.domain.model.CatalogSorting
import com.mskd.flux.features.catalog.domain.model.CatalogSortingOption

@Immutable
data class CatalogUiState(
    val state: CatalogState = CatalogState.Loading(),
)

sealed class CatalogState {

    data object Error: CatalogState()

    @Immutable
    data class Loading(val progress: Float = 0f): CatalogState()

    @Immutable
    data class Content(
        val artworks: List<Artwork> = emptyList(),
        val lastWatchedMediaIds: List<Long> = emptyList(),
        val isRefreshing: Boolean = true,
        val tokenIsMissing: Boolean = false,
        val sorting: CatalogSorting = CatalogSorting()
    ): CatalogState()

}

sealed interface CatalogIntent {

    // Navigation
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): CatalogIntent
    data class OnCategoryTap(val category: ContentType): CatalogIntent
    data object SyncCatalog: CatalogIntent
    data object OnSearchTap: CatalogIntent
    data object OnSettingsTap: CatalogIntent
    data object OnHowToTap: CatalogIntent
    data object OnSourcesTap: CatalogIntent
    data object OnTokenTap: CatalogIntent

    // Sort
    data class SelectSortingOption(val option: CatalogSortingOption): CatalogIntent
    data class ShowSortingOptions(val show: Boolean): CatalogIntent
}

sealed interface CatalogEvent {
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): CatalogEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): CatalogEvent
    data class NavigateToCategory(val category: ContentType): CatalogEvent
    data object NavigateToUnknown: CatalogEvent
    data object NavigateToSearch: CatalogEvent
    data object NavigateToSettings: CatalogEvent
    data object NavigateToToken: CatalogEvent
    data object NavigateToHowTo: CatalogEvent
    data object NavigateToSources: CatalogEvent
}