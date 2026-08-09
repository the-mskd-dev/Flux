package com.mskd.flux.features.search.presentation

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SearchUIState(
    val artworks: ImmutableList<Artwork> = persistentListOf(),
    val autoKeyboard: Boolean = true,
    val availableGenres: ImmutableList<Genre> = persistentListOf(),
    val actions: SearchUserActions = SearchUserActions()
)

data class SearchUserActions(
    val input: String = "",
    val selectedType: ContentType? = null,
    val selectedGenres: ImmutableList<Int> = persistentListOf(),
    val showGenresSelection: Boolean = false
)

sealed interface SearchIntent {
    object OnBackTap: SearchIntent
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int?): SearchIntent
    data class DoSearch(val query: String) : SearchIntent
    data class FilterOnType(val contentType: ContentType) : SearchIntent

    data class ShowGenresSelection(val show: Boolean) : SearchIntent
    data class SelectGenre(val genre: Genre) : SearchIntent
    data object ClearGenres : SearchIntent
}

sealed interface SearchEvent {
    object BackToPreviousScreen: SearchEvent
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): SearchEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): SearchEvent
}