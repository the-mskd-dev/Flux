package com.kaem.flux.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = true,
    val artworks: List<FluxArtworkSummary> = emptyList(),
    val episodes: List<FluxEpisode> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {


    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init { refreshFiles() }

    fun refreshFiles() = viewModelScope.launch {

        _uiState.update {

            it.copy(isLoading = true)

        }

        repository.getLibrary().collect { result ->

            val artworks = result.getOrNull().orEmpty()
            val episodes = repository.getEpisodes()

            _uiState.update {

                it.copy(
                    artworks = artworks,
                    episodes = episodes,
                    isLoading = false
                )

            }

        }

    }

}