package com.kaem.flux.screens.search

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ContentType

@Immutable
data class SearchUIState(
    val searchWord: String = "",
    val artworks: List<Artwork> = emptyList(),
    val contentType: ContentType? = null
) {

    val filteredArtworks get() = artworks
        .filter { if (contentType != null) it.type == contentType else true }
        .filter { it.title.contains(searchWord, true) }
}

sealed class SearchIntent {
    object OnBackTap: SearchIntent()
    data class OnArtworkTap(val artworkId: Long): SearchIntent()
    data class DoSearch(val query: String) : SearchIntent()
    data class FilterOnType(val contentType: ContentType) : SearchIntent()
}

sealed class SearchEvent {
    object BackToPreviousScreen: SearchEvent()
    data class NavigateToMedia(val mediaId: Long): SearchEvent()
}