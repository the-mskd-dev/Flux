package com.kaem.flux.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.media.MediaOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUIState(
    val searchWord: String = "",
    val overviews: List<MediaOverview> = emptyList()
) {

    val filteredOverviews get() = overviews.filter { it.title.contains(searchWord, true) }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.catalogFlow.collect { library ->
                _uiState.update { it.copy(overviews = library.mediaOverviews) }
            }
        }
    }

    fun updateSearchWord(value: String) {
        _uiState.update { it.copy(searchWord = value) }
    }

}