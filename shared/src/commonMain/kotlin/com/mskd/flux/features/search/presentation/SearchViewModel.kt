package com.mskd.flux.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    withType: ContentType? = null,
    withGenre: Genre? = null,
    private val database: DatabaseRepository,
    private val details: DetailsRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _userActions = MutableStateFlow(SearchUserActions(
        selectedType = withType,
        selectedGenres = withGenre?.let { persistentListOf(it.id) } ?: persistentListOf()
    ))

    val uiState = combine(
        database.flowArtworks(),
        details.flowGenres(),
        settings.flow,
        _userActions
    ) { artworks, genres, settings, actions ->

        val allGenresIds = artworks.flatMap { it.genreIds }.distinct()

        val filteredArtworks = artworks
            .asSequence()
            .filter { !it.isUnknown }
            .filter { actions.selectedType == null || it.type == actions.selectedType }
            .filter { actions.selectedGenres.isEmpty() || actions.selectedGenres.any { id -> id in it.genreIds } }
            .filter { it.title.contains(actions.input, ignoreCase = true) }
            .sortedBy { it.title }
            .toImmutableList()

        SearchUIState(
            artworks = filteredArtworks,
            autoKeyboard = settings.autoKeyboard,
            availableGenres = genres.filter { allGenresIds.contains(it.id) }.toImmutableList(),
            actions = actions
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUIState()
    )

    private val _event = MutableSharedFlow<SearchEvent>()
    val event = _event.asSharedFlow()

    fun handleIntent(intent: SearchIntent) = viewModelScope.launch {
        when (intent) {
            SearchIntent.OnBackTap -> _event.emit(SearchEvent.BackToPreviousScreen)
            is SearchIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is SearchIntent.FilterOnType -> filterOnType(type = intent.contentType)
            is SearchIntent.DoSearch -> doSearch(query = intent.query)

            // Genres
            SearchIntent.ClearGenres -> clearGenres()
            is SearchIntent.SelectGenre -> selectGenre(genre = intent.genre)
            is SearchIntent.ShowGenresSelection -> showGenresSelection(show = intent.show)
        }
    }

    private suspend fun onArtworkTap(artwork: Artwork, rgb: Int?) {

        val event = when(artwork.type) {
            ContentType.SHOW -> SearchEvent.NavigateToShow(artworkId = artwork.id, rgb = rgb)
            ContentType.MOVIE -> SearchEvent.NavigateToMovie(artworkId = artwork.id, rgb = rgb)
        }

        _event.emit(event)

    }

    private fun doSearch(query: String) {
        _userActions.update { it.copy(input = query) }
    }

    private fun filterOnType(type: ContentType) {
        _userActions.update {
            if (it.selectedType == type)
                it.copy(selectedType = null)
            else
                it.copy(selectedType = type)
        }

    }

    private fun clearGenres() {
        _userActions.update { it.copy(selectedGenres = persistentListOf()) }
    }

    private fun selectGenre(genre: Genre) {

        val id = genre.id

        _userActions.update {
            val selectedGenres = if (it.selectedGenres.contains(id))
                it.selectedGenres.minus(id)
            else
                it.selectedGenres.plus(id)
            it.copy(selectedGenres = selectedGenres.toImmutableList())
        }
    }

    private fun showGenresSelection(show: Boolean) {
        _userActions.update { it.copy(showGenresSelection = show) }
    }


}