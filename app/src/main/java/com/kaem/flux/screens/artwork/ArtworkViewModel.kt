package com.kaem.flux.screens.artwork

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkType
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ArtworkUiState(
    val artwork: Artwork = Artwork(),
    val expandedEpisodeId: Int? = null,
    val currentSeason: Int = -1
) {

    val artworkDetails: ArtworkInfo? = when (artwork.type) {
        is ArtworkType.MOVIE -> artwork.type.movie
        is ArtworkType.SHOW -> artwork.type.currentEpisode
    }

}

@HiltViewModel
class ArtworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["artworkId"])

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState.asStateFlow()

    init {

        getArtworks(artworkId)

    }

    private fun getArtworks(id: Int) {

        val libraryContent = repository.libraryContent.value

        val artwork = libraryContent?.artworks?.find { it.id == id } ?: return

        _uiState.value = ArtworkUiState(
            artwork = artwork,
            currentSeason = (artwork.type as? ArtworkType.SHOW)?.currentEpisode?.season ?: -1
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

    fun changeWatchStatus(episode: Episode) {
        episode.status = if (episode.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH
        viewModelScope.launch { repository.saveEpisodes(listOf(episode)) }
    }

}