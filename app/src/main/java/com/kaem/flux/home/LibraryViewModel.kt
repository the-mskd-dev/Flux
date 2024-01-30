package com.kaem.flux.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.data.repository.SortOrder
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
    val sortOrder: SortOrder = SortOrder.RELEASE_DATE
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val dataStorePreferencesFlow = dataStoreRepository.preferencesFlow

    private val libraryUiStateFlow = combine(
        repository.libraryContent,
        dataStorePreferencesFlow
    ) { libraryContent, preferences ->

        isLoading = false

        return@combine LibraryUiState(
            artworks = sortedArtworks(
                artworks = libraryContent.artworks,
                episodes = libraryContent.episodes,
                sortOrder = preferences.sortOrder
            ),
            episodes = libraryContent.episodes,
            sortOrder = preferences.sortOrder
        )

    }
    val libraryUiState = libraryUiStateFlow.asLiveData()

    private fun sortedArtworks(
        artworks: List<FluxArtworkSummary>,
        episodes: List<FluxEpisode>,
        sortOrder: SortOrder
    ) : List<FluxArtworkSummary> {

        return when (sortOrder) {
            SortOrder.NAME -> artworks.sortedBy { it.title }
            SortOrder.RELEASE_DATE -> artworks.sortedByDescending { it.releaseDate }
            SortOrder.ADDED_DATE -> {

                artworks.sortedByDescending { artwork ->

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

    }

    fun applySort(sortOrder: SortOrder) {
        viewModelScope.launch { dataStoreRepository.updateSortOrder(sortOrder) }
    }

    fun getLibrary() {

        viewModelScope.launch {

            isLoading = true
            repository.getLibrary()

        }

    }

}