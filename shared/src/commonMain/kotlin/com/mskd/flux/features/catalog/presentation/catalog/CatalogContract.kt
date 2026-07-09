package com.mskd.flux.features.catalog.presentation.catalog

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.utils.FluxSnackbar

@Immutable
data class CatalogUiState(
    val state: CatalogState = CatalogState.Loading(),
    val snackbarState: FluxSnackbar? = null
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
    ): CatalogState()

}

sealed class CatalogIntent {
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): CatalogIntent()
    data class OnCategoryTap(val category: ContentType): CatalogIntent()
    data object SyncCatalog: CatalogIntent()
    object OnSearchTap: CatalogIntent()
    object OnSettingsTap: CatalogIntent()
    object OnHowToTap: CatalogIntent()
    object OnSnackbarActionTap: CatalogIntent()
    object OnDismissSnackbar: CatalogIntent()
}

sealed class CatalogEvent {
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): CatalogEvent()
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): CatalogEvent()
    data class NavigateToCategory(val category: ContentType): CatalogEvent()
    object NavigateToUnknown: CatalogEvent()
    object NavigateToSearch: CatalogEvent()
    object NavigateToSettings: CatalogEvent()
    object NavigateToToken: CatalogEvent()
    object NavigateToHowTo: CatalogEvent()
}