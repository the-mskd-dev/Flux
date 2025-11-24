package com.kaem.flux.screens.search

import com.kaem.flux.model.media.MediaOverview

data class SearchUIState(
    val searchWord: String = "",
    val overviews: List<MediaOverview> = emptyList()
) {

    val filteredOverviews get() = overviews.filter { it.title.contains(searchWord, true) }
}

sealed class SearchIntent {
    object OnBackTap: SearchIntent()
    data class OnMediaTap(val mediaId: Long): SearchIntent()
    data class DoSearch(val query: String) : SearchIntent()
}

sealed class SearchEvent {
    object BackToPreviousScreen: SearchEvent()
    data class NavigateToMedia(val mediaId: Long): SearchEvent()
}