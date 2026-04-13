package com.mskd.flux.screens.home

import androidx.compose.runtime.Immutable
import com.mskd.flux.R
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType

@Immutable
data class HomeUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val artworks: List<Artwork> = emptyList(),
    val lastWatchedMediaIds: List<Long> = emptyList(),
    val isRefreshing: Boolean = true,
    val snackbarState: SnackbarState? = null
) {


    sealed class SnackbarState(val message: Int, val action: Int) {
        data object Token: SnackbarState(message = R.string.snackbar_add_api_key, action = R.string.add)
        data object Tutorial: SnackbarState(message = R.string.snackbar_see_tuto, action = R.string.see)
    }

}

sealed class HomeIntent {
    data class OnArtworkTap(val artworkId: Long): HomeIntent()
    data class OnCategoryTap(val category: ContentType): HomeIntent()
    data object SyncCatalog: HomeIntent()
    object OnSearchTap: HomeIntent()
    object OnSettingsTap: HomeIntent()
    object OnHowToTap: HomeIntent()
    object OnSnackbarActionTap: HomeIntent()
    object OnDismissSnackbar: HomeIntent()
}

sealed class HomeEvent {
    data class NavigateToArtwork(val artworkId: Long): HomeEvent()
    data class NavigateToCategory(val category: ContentType): HomeEvent()
    object NavigateToUnknown: HomeEvent()
    object NavigateToSearch: HomeEvent()
    object NavigateToSettings: HomeEvent()
    object NavigateToToken: HomeEvent()
    object NavigateToHowTo: HomeEvent()
}