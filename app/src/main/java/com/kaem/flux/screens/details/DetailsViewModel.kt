package com.kaem.flux.screens.details

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkDetails
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.model.flux.FluxStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class DetailsUiState(
    val artwork: FluxArtwork? = null,
    val episodes: List<FluxEpisode> = emptyList(),
    val expandedEpisodeId: Int? = null,
    val currentEpisode: FluxEpisode? = null,
    val currentSeason: Int = -1
) {

    val artworkDetails: FluxArtworkDetails? = currentEpisode ?: artwork as? FluxMovie

    val description: String? = when (artwork) {
        is FluxShow -> currentEpisode?.description
        is FluxMovie -> artwork.description
        else -> null
    }

}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["artworkId"])

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {

        getArtworks(artworkId)

    }

    private fun getArtworks(id: Int) {

        val libraryContent = repository.libraryContent.value

        val artwork = libraryContent?.artworks?.find { it.id == id } ?: return
        val episodes = if (artwork is FluxShow)  libraryContent.episodes.filter { it.showId == id } else emptyList()
        val selectedEpisode = episodes.lastOrNull { it.status == FluxStatus.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == FluxStatus.TO_WATCH }
            ?: episodes.firstOrNull()

        _uiState.value = DetailsUiState(
            artwork = artwork,
            episodes = ArrayList(episodes),
            currentEpisode = selectedEpisode,
            currentSeason = selectedEpisode?.season ?: -1
        )

    }

    fun selectSeason(season: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentSeason = season)
        }
    }

    fun expandEpisodeDetails(id: Int) {
        _uiState.update { currentState ->
            currentState.copy(expandedEpisodeId = if (currentState.expandedEpisodeId == id) null else id)
        }
    }

    fun checkEpisodesAsWatched(episode: FluxEpisode) {

        val episodesToSave = arrayListOf<FluxEpisode>()

        val newEpisodes = buildList {

            uiState.value.episodes.forEach {

                if (it.season < episode.season || (it.season == episode.season && it.number <= episode.number)) {
                    val newEpisode = it.copy(status = FluxStatus.WATCHED)
                    add(newEpisode)
                    episodesToSave.add(newEpisode)
                } else {
                    add(it)
                }

            }

        }

        viewModelScope.launch { repository.saveEpisodes(episodesToSave) }

        _uiState.update { currentState ->
            currentState.copy(episodes = newEpisodes)
        }

    }

    fun changeWatchStatus(episode: FluxEpisode) {
        episode.status = if (episode.status != FluxStatus.WATCHED) FluxStatus.WATCHED else FluxStatus.TO_WATCH
        viewModelScope.launch { repository.saveEpisodes(listOf(episode)) }
    }

}