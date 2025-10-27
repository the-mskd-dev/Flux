package com.kaem.flux.screens.search

sealed class SearchEvent {
    object BackToPreviousScreen: SearchEvent()
    data class NavigateToMedia(val mediaId: Long): SearchEvent()
}