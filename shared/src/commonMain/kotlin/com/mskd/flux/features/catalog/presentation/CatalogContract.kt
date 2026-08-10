package com.mskd.flux.features.catalog.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode

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
        val genres: List<Genre> = emptyList(),
        val lastWatchedMediaIds: List<Long> = emptyList(),
        val isRefreshing: Boolean = true,
        val tokenIsMissing: Boolean = false,

        // Sort
        val sortingMode: CatalogSortingMode = CatalogSortingMode.LAST_MODIFICATION,
        val showSortingSheet: Boolean = false,

        // View
        val viewMode: CatalogViewMode = CatalogViewMode.BY_TYPE,
        val showViewSheet: Boolean = false,
    ): CatalogState()

}

sealed interface CatalogIntent {

    // Navigation
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): CatalogIntent
    data class OnGenreTap(val genre: Genre): CatalogIntent
    data class OnCategoryTap(val category: ContentType): CatalogIntent
    data object SyncCatalog: CatalogIntent
    data object OnSearchTap: CatalogIntent
    data object OnSettingsTap: CatalogIntent
    data object OnHowToTap: CatalogIntent
    data object OnSourcesTap: CatalogIntent
    data object OnTokenTap: CatalogIntent

    // Sort
    data class SelectSortingMode(val mode: CatalogSortingMode): CatalogIntent
    data class ShowSortingModes(val show: Boolean): CatalogIntent

    // View mode
    data class SelectViewMode(val mode: CatalogViewMode): CatalogIntent
    data class ShowViewModes(val show: Boolean): CatalogIntent
}

sealed interface CatalogEvent {
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): CatalogEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): CatalogEvent
    data object NavigateToUnknown: CatalogEvent
    data class NavigateToSearch(val category: ContentType? = null, val genre: Genre? = null): CatalogEvent
    data object NavigateToSettings: CatalogEvent
    data object NavigateToToken: CatalogEvent
    data object NavigateToHowTo: CatalogEvent
    data object NavigateToSources: CatalogEvent
}