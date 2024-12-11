package com.kaem.flux.screens.artwork

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.model.flux.ContentType
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
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
    val screen: ArtworkUiType = ArtworkUiType.LOADING,
    val expandedEpisodeId: Long? = null,
    val currentSeason: Int = -1
) {

    val artworkDetails: ArtworkInfo? = when (screen) {
        is ArtworkUiType.MOVIE -> screen.movie
        is ArtworkUiType.SHOW -> screen.currentEpisode
        else -> null
    }

}

sealed class ArtworkUiType {
    data object LOADING : ArtworkUiType()
    data object ERROR : ArtworkUiType()
    data class MOVIE(val movie: Movie) : ArtworkUiType()
    data class SHOW(val episodes: List<Episode> = emptyList()) : ArtworkUiType() {
        val currentEpisode get() = episodes.lastOrNull { it.status == Status.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
            ?: episodes.firstOrNull()
    }
}

@HiltViewModel
class ArtworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Long = checkNotNull(savedStateHandle["artworkId"])

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState.asStateFlow()

    init { getArtworks(artworkId) }

    private fun getArtworks(id: Long) = viewModelScope.launch {

        val libraryContent = repository.libraryContent.value

        val artwork = libraryContent?.artworks?.find { it.id == id }

        when (artwork?.type) {
            ContentType.MOVIE -> {
                val movie = repository.getMovie(artworkId)
                _uiState.value = ArtworkUiState(
                    artwork = artwork,
                    screen = ArtworkUiType.MOVIE(movie)
                )

            }
            ContentType.SHOW -> {
                val episodes = repository.getEpisodes(artworkId)
                ArtworkUiType.SHOW(episodes).also {
                    _uiState.value = ArtworkUiState(
                        artwork = artwork,
                        screen = it,
                        currentSeason = it.currentEpisode?.season ?: -1
                    )
                }
            }
            else -> {
                _uiState.value = ArtworkUiState(
                    artwork = artwork ?: Artwork(),
                    screen = ArtworkUiType.ERROR,
                )
            }
        }



    }

    fun selectSeason(season: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentSeason = season)
        }
    }

    fun expandEpisodeDetails(id: Long) {
        _uiState.update { currentState ->
            currentState.copy(expandedEpisodeId = if (currentState.expandedEpisodeId == id) null else id)
        }
    }

    fun changeWatchStatus(episode: Episode) {
        episode.status = if (episode.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH
        viewModelScope.launch { repository.saveEpisode(episode) }
    }

}