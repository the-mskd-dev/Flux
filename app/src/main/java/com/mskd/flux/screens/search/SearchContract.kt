package com.mskd.flux.screens.search

import androidx.compose.runtime.Immutable
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType

@Immutable
data class SearchUIState(
    val searchWord: String = "",
    val artworks: List<Artwork> = emptyList(),
    val contentType: ContentType? = null,
    val autoKeyboard: Boolean = true
) {

    val filteredArtworks get() = artworks
        .filter { if (contentType != null) it.type == contentType else true }
        .filter { it.title.contains(searchWord, true) }
}

sealed class SearchIntent {
    object OnBackTap: SearchIntent()
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int?): SearchIntent()
    data class DoSearch(val query: String) : SearchIntent()
    data class FilterOnType(val contentType: ContentType) : SearchIntent()
}

sealed class SearchEvent {
    object BackToPreviousScreen: SearchEvent()
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): SearchEvent()
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): SearchEvent()
}