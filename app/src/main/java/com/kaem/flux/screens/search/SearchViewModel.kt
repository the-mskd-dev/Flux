package com.kaem.flux.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SearchEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.catalogFlow.collect { library ->
                _uiState.update { it.copy(overviews = library.mediaOverviews) }
            }
        }
    }

    fun handleIntent(intent: SearchIntent) = viewModelScope.launch {
        when (intent) {
            SearchIntent.OnBackTap -> _event.emit(SearchEvent.BackToPreviousScreen)
            is SearchIntent.OnMediaTap -> _event.emit(SearchEvent.NavigateToMedia(intent.mediaId))
            is SearchIntent.DoSearch -> doSearch(query = intent.query)
        }
    }

    private fun doSearch(query: String) {
        _uiState.update { it.copy(searchWord = query) }
    }

}