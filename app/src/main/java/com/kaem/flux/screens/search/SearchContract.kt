package com.kaem.flux.screens.search

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.MediaOverview

@Immutable
data class SearchUIState(
    val searchWord: String = "",
    val overviews: List<MediaOverview> = emptyList(),
    val contentType: ContentType? = null
) {

    val filteredOverviews get() = overviews
        .filter { if (contentType != null) it.type == contentType else true }
        .filter { it.title.contains(searchWord, true) }
}

sealed class SearchIntent {
    object OnBackTap: SearchIntent()
    data class OnMediaTap(val mediaId: Long): SearchIntent()
    data class DoSearch(val query: String) : SearchIntent()
    data class FilterOnType(val contentType: ContentType) : SearchIntent()
}

sealed class SearchEvent {
    object BackToPreviousScreen: SearchEvent()
    data class NavigateToMedia(val mediaId: Long): SearchEvent()
}