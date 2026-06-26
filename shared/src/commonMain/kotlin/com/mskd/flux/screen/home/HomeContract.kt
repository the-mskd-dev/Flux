package com.mskd.flux.screen.home

import androidx.compose.runtime.Immutable
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.utils.FluxSnackbar

@Immutable
data class HomeUiState(
    val state: HomeState = HomeState.Loading(),
    val snackbarState: FluxSnackbar? = null
)

sealed class HomeState {

    data object Error: HomeState()

    @Immutable
    data class Loading(val progress: Float = 0f): HomeState()

    @Immutable
    data class Content(
        val artworks: List<Artwork> = emptyList(),
        val lastWatchedMediaIds: List<Long> = emptyList(),
        val isRefreshing: Boolean = true,
    ): HomeState()

}

sealed class HomeIntent {
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): HomeIntent()
    data class OnCategoryTap(val category: ContentType): HomeIntent()
    data object SyncCatalog: HomeIntent()
    object OnSearchTap: HomeIntent()
    object OnSettingsTap: HomeIntent()
    object OnHowToTap: HomeIntent()
    object OnSnackbarActionTap: HomeIntent()
    object OnDismissSnackbar: HomeIntent()
}

sealed class HomeEvent {
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): HomeEvent()
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): HomeEvent()
    data class NavigateToCategory(val category: ContentType): HomeEvent()
    object NavigateToUnknown: HomeEvent()
    object NavigateToSearch: HomeEvent()
    object NavigateToSettings: HomeEvent()
    object NavigateToToken: HomeEvent()
    object NavigateToHowTo: HomeEvent()
}