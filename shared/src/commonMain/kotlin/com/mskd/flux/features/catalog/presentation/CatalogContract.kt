package com.mskd.flux.features.catalog.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType

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
        val tokenIsMissing: Boolean = false
    ): CatalogState()

}

sealed interface CatalogIntent {
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): CatalogIntent
    data class OnCategoryTap(val category: ContentType): CatalogIntent
    data object SyncCatalog: CatalogIntent
    object OnSearchTap: CatalogIntent
    object OnSettingsTap: CatalogIntent
    object OnHowToTap: CatalogIntent
    object OnSourcesTap: CatalogIntent
    object OnTokenTap: CatalogIntent
}

sealed interface CatalogEvent {
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): CatalogEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): CatalogEvent
    data class NavigateToCategory(val category: ContentType): CatalogEvent
    object NavigateToUnknown: CatalogEvent
    object NavigateToSearch: CatalogEvent
    object NavigateToSettings: CatalogEvent
    object NavigateToToken: CatalogEvent
    object NavigateToHowTo: CatalogEvent
    object NavigateToSources: CatalogEvent
}