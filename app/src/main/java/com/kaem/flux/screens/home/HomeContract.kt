package com.kaem.flux.screens.home

import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.MediaOverview

data class HomeUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val overviews: List<MediaOverview> = emptyList(),
    val lastWatchedMediaIds: List<Long> = emptyList(),
    val isRefreshing: Boolean = true
)

sealed class HomeIntent {
    data class OnMediaTap(val mediaId: Long): HomeIntent()
    data class OnCategoryTap(val category: ContentType): HomeIntent()
    data class OnSyncTap(val manualSync: Boolean): HomeIntent()
    object OnSearchTap: HomeIntent()
    object OnSettingsTap: HomeIntent()
    object OnHowToTap: HomeIntent()
    object OnPermissionTap: HomeIntent()
}

sealed class HomeEvent {
    data class NavigateToMedia(val mediaId: Long): HomeEvent()
    data class NavigateToCategory(val category: ContentType): HomeEvent()
    object NavigateToSearch: HomeEvent()
    object NavigateToSettings: HomeEvent()
    object NavigateToHowTo: HomeEvent()
    object OpenPermissionDialog: HomeEvent()
}