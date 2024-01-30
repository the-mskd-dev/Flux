package com.kaem.flux.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.data.repository.SortOrder
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = true,
    val artworks: List<FluxArtworkSummary> = emptyList(),
    val episodes: List<FluxEpisode> = emptyList(),
    val sortOrder: SortOrder = SortOrder.RELEASE_DATE
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private val dataStorePreferencesFlow = dataStoreRepository.preferencesFlow

    private val libraryUiStateFlow = combine(
        repository.artworks,
        repository.episodes,
        dataStorePreferencesFlow
    ) { artworks, episodes, preferences ->
        return@combine LibraryUiState(
            artworks = sortedArtworks(artworks = artworks, sortOrder = preferences.sortOrder),
            episodes = episodes,
            isLoading = false,
            sortOrder = preferences.sortOrder
        )
    }
    val libraryUiState = libraryUiStateFlow.asLiveData()

    private fun sortedArtworks(
        artworks: List<FluxArtworkSummary>,
        sortOrder: SortOrder
    ) : List<FluxArtworkSummary> {

        return when (sortOrder) {
            SortOrder.NAME -> artworks.sortedBy { it.title }
            SortOrder.RELEASE_DATE -> artworks.sortedByDescending { it.releaseDate }
        }

    }

    fun applySort(sortOrder: SortOrder) {
        viewModelScope.launch { dataStoreRepository.updateSortOrder(sortOrder) }
    }

    fun getLibrary() {
        viewModelScope.launch {
            repository.getLibrary()
        }
    }
    /*init {
        refreshFiles()
    }

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

    }*/

}