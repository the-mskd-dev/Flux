package com.kaem.flux.screens.home

import com.kaem.flux.model.media.ContentType

sealed class HomeEvent {
    data class NavigateToMedia(val mediaId: Long): HomeEvent()
    data class NavigateToCategory(val category: ContentType): HomeEvent()
    object NavigateToSearch: HomeEvent()
    object NavigateToSettings: HomeEvent()
    object NavigateToHowTo: HomeEvent()
    object OpenPermissionDialog: HomeEvent()
}