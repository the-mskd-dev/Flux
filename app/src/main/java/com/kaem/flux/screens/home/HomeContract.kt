package com.kaem.flux.screens.home

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ContentType

@Immutable
data class HomeUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val artworks: List<Artwork> = emptyList(),
    val lastWatchedMediaIds: List<Long> = emptyList(),
    val isRefreshing: Boolean = true
)

sealed class HomeIntent {
    data class OnArtworkTap(val artworkId: Long): HomeIntent()
    data class OnCategoryTap(val category: ContentType): HomeIntent()
    data class OnSyncTap(val manualSync: Boolean): HomeIntent()
    object OnSearchTap: HomeIntent()
    object OnSettingsTap: HomeIntent()
    object OnHowToTap: HomeIntent()
}

sealed class HomeEvent {
    data class NavigateToArtwork(val mediaId: Long): HomeEvent()
    data class NavigateToCategory(val category: ContentType): HomeEvent()
    object NavigateToSearch: HomeEvent()
    object NavigateToSettings: HomeEvent()
    object NavigateToHowTo: HomeEvent()
}