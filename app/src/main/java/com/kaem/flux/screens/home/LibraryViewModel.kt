package com.kaem.flux.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val artworks: List<Artwork> = emptyList(),
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

        return@combine libraryContent?.let {
            LibraryUiState(
                artworks = libraryContent.artworks,
                lastWatchedArtworkIds = preferences.lastWatchedIds,
                isLoading = libraryContent.isLoading
            )
        }

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
        artworks: List<Artwork>
    ) : List<Artwork> {

        return artworks.sortedByDescending { artwork ->

            when (artwork.content) {

                is ArtworkContent.MOVIE -> {
                    artwork.content.movie.file.addedDate
                }

                is ArtworkContent.SHOW -> {
                    artwork.content.episodes.maxOf { it.file.addedDate }
                }

            }

        }

    }

}