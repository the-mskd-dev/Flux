package com.kaem.flux.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val artworks: List<FluxArtworkSummary> = emptyList(),
    val episodes: List<FluxEpisode> = emptyList(),
    val lastWatchedArtworkIds: List<Int> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private val dataStorePreferencesFlow = dataStoreRepository.preferencesFlow

    private val libraryUiStateFlow = combine(
        repository.libraryContent,
        dataStorePreferencesFlow
    ) { libraryContent, preferences ->

        return@combine LibraryUiState(
            artworks = libraryContent.artworks,
            episodes = libraryContent.episodes,
            lastWatchedArtworkIds = preferences.lastWatchedIds,
            isLoading = libraryContent.isLoading
        )

    }
    val libraryUiState = libraryUiStateFlow.asLiveData()

    fun getLibrary() {
        viewModelScope.launch {
            repository.getLibrary()
        }
    }

    fun addWatchedArtwork(id: Int) {
        viewModelScope.launch {
            dataStoreRepository.addWatchedArtwork(id)
        }
    }

    fun getArtworksByAddedDate(
        artworks: List<FluxArtworkSummary>,
        episodes: List<FluxEpisode>,
    ) : List<FluxArtworkSummary> {

        return artworks.sortedByDescending { artwork ->

            val date = when (artwork) {

                is FluxMovie -> {
                    artwork.file.addedDate
                }

                is FluxShow -> {
                    episodes.filter { artwork.id == it.showId }.maxOf { it.file.addedDate }
                }

                else -> artwork.releaseDate
            }

            date

        }

    }

}