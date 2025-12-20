package com.kaem.flux.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.artwork.ContentType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    @Assisted contentType: ContentType? = null,
    private val repository: CatalogRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(contentType: ContentType?): SearchViewModel
    }

    private val _uiState = MutableStateFlow(SearchUIState(contentType = contentType))
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SearchEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.catalogFlow.collect { library ->
                _uiState.update { it.copy(artworks = library.artworks) }
            }
        }
    }

    fun handleIntent(intent: SearchIntent) = viewModelScope.launch {
        when (intent) {
            SearchIntent.OnBackTap -> _event.emit(SearchEvent.BackToPreviousScreen)
            is SearchIntent.OnArtworkTap -> _event.emit(SearchEvent.NavigateToMedia(intent.artworkId))
            is SearchIntent.FilterOnType -> filterOnType(type = intent.contentType)
            is SearchIntent.DoSearch -> doSearch(query = intent.query)
        }
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