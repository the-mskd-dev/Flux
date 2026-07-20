package com.mskd.flux.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchViewModel(
    contentType: ContentType? = null,
    private val database: DatabaseRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUIState(
            contentType = contentType,
            autoKeyboard = runBlocking { settingsDataStore.flow.first().autoKeyboard } && contentType == null
        )
    )
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SearchEvent>()
    val event = _event.asSharedFlow()

    init {

        viewModelScope.launch {
            database.flowArtworks().collect { artworks ->
                _uiState.update {
                    it.copy(artworks = artworks.filter { artworks -> !artworks.isUnknown },)
                }
            }
        }
    }

    fun handleIntent(intent: SearchIntent) = viewModelScope.launch {
        when (intent) {
            SearchIntent.OnBackTap -> _event.emit(SearchEvent.BackToPreviousScreen)
            is SearchIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is SearchIntent.FilterOnType -> filterOnType(type = intent.contentType)
            is SearchIntent.DoSearch -> doSearch(query = intent.query)
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
        _uiState.update { it.copy(searchWord = query) }
    }

    private fun filterOnType(type: ContentType) {
        _uiState.update {
            if (it.contentType == type)
                it.copy(contentType = null)
            else
                it.copy(contentType = type)

        }

    }
}