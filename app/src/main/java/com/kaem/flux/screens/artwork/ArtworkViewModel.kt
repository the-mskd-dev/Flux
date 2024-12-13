package com.kaem.flux.screens.artwork

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.model.WatchTime
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import com.kaem.flux.model.flux.Status
import com.kaem.flux.utils.timeDescription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ArtworkUiState(
    val artwork: Artwork = Artwork(),
    val screen: Screen = Screen.LOADING,
    val expandedEpisodeId: Long? = null,
    val currentSeason: Int = -1,
    val selectedArtwork: ArtworkInfo? = null
) {

    val artworkDetails: ArtworkInfo? = when (screen) {
        is Screen.MOVIE -> screen.movie
        is Screen.SHOW -> screen.currentEpisode
        else -> null
    }

    sealed class Screen {
        data object LOADING : Screen()
        data object ERROR : Screen()
        data class MOVIE(val movie: Movie) : Screen()
        data class SHOW(val episodes: List<Episode> = emptyList()) : Screen() {
            val currentEpisode get() = episodes.lastOrNull { it.status == Status.IS_WATCHING }
                ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
                ?: episodes.firstOrNull()
        }
    }

}

@HiltViewModel
class ArtworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtworkRepository
) : ViewModel() {

    private val artworkId: Long = checkNotNull(savedStateHandle["artworkId"])

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState.asStateFlow()

    init { getArtworks(artworkId) }

    private fun getArtworks(id: Long) = viewModelScope.launch {

        val (artwork, movie, episodes) = repository.getArtwork(id)

        when {
            movie != null -> {
                _uiState.value = ArtworkUiState(
                    artwork = artwork,
                    screen = ArtworkUiState.Screen.MOVIE(movie)
                )
            }
            !episodes.isNullOrEmpty() -> {
                val screen = ArtworkUiState.Screen.SHOW(episodes)
                _uiState.value = ArtworkUiState(
                    artwork = artwork,
                    screen = screen,
                    currentSeason = screen.currentEpisode?.season ?: -1
                )
            }
            else -> {
                ArtworkUiState(
                    artwork = artwork,
                    screen = ArtworkUiState.Screen.ERROR,
                )
            }

        }

    }

    fun selectArtwork(artworkInfo: ArtworkInfo?) {
        _uiState.update { currentState ->
            currentState.copy(selectedArtwork = artworkInfo)
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

        // Change status
        val updatedEpisode = episode.copy(status = if (episode.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH)

        // Update list
        val episodes = (_uiState.value.screen as? ArtworkUiState.Screen.SHOW)?.episodes.orEmpty().toMutableList()
        episodes.replaceAll { if (it.id == episode.id) updatedEpisode else it }
        _uiState.update { currentState ->
            currentState.copy(
                screen = ArtworkUiState.Screen.SHOW(episodes)
            )
        }

        // Save status in DB
        viewModelScope.launch { repository.saveEpisode(episode) }

        Log.i("ArtworkViewModel", "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")
    }

    fun saveCurrentTime(time: Long) = viewModelScope.launch {

        uiState.value.let { state ->

            state.selectedArtwork?.let { artworkInfo ->
                artworkInfo.currentTime = time
                val watchTime = WatchTime.fromTime(time)
                artworkInfo.status = if (watchTime.timeInMin >= artworkInfo.duration) Status.WATCHED else Status.IS_WATCHING
            }

            when (state.selectedArtwork) {

                is Movie -> repository.saveMovie(state.selectedArtwork)

                is Episode -> repository.saveEpisode(state.selectedArtwork)

                else -> {}

            }

            Log.i("ArtworkViewModel", "${state.artwork.title} saved at ${time.timeDescription}")

        }

    }

}