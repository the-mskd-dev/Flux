package com.kaem.flux.screens.artwork

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.utils.extensions.getPreviousEpisodesFor
import com.kaem.flux.utils.extensions.inMinutes
import com.kaem.flux.utils.extensions.timeDescription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes


data class ArtworkUiState(
    val overview: ArtworkOverview = ArtworkOverview(),
    val screen: ScreenState = ScreenState.LOADING,
    val selectedArtwork: Artwork? = null,
    val episodes: List<Episode> = emptyList(),
    val currentSeason: Int = -1,
    val showPlayer: Boolean = false,
    val showStatusDialog: Boolean = false
)

@HiltViewModel
class ArtworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtworkRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val artworkId: Long = checkNotNull(savedStateHandle["artworkId"])

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState.asStateFlow()

    init { getArtworks(artworkId) }

    private fun getArtworks(id: Long) = viewModelScope.launch {

        val (overview, movie, episodes) = repository.getArtwork(id)

        when {
            movie != null -> {
                _uiState.value = ArtworkUiState(
                    overview = overview,
                    screen = ScreenState.CONTENT,
                    selectedArtwork = movie
                )
            }
            !episodes.isNullOrEmpty() -> {
                
                val currentEpisode = episodes.lastOrNull { it.status == Status.IS_WATCHING }
                    ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
                    ?: episodes.first()

                _uiState.value = ArtworkUiState(
                    overview = overview,
                    screen = ScreenState.CONTENT,
                    episodes = episodes,
                    currentSeason = currentEpisode.season,
                    selectedArtwork = currentEpisode
                )
            }
            else -> {
                _uiState.value = ArtworkUiState(
                    screen = ScreenState.ERROR
                )
            }

        }

    }

    fun selectArtwork(artwork: Artwork?) {
        _uiState.update { currentState ->
            currentState.copy(selectedArtwork = artwork)
        }
    }

    fun selectSeason(season: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentSeason = season)
        }
    }

    fun showPlayer(show: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showPlayer = show)
        }
    }

    fun showStatusDialog(show: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showStatusDialog = show)
        }
    }

    fun changeWatchStatus() {

        val artwork = uiState.value.selectedArtwork ?: return

        val newStatus = if (artwork.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        if (
            newStatus == Status.WATCHED
            && artwork is Episode
            && _uiState.value.episodes.getPreviousEpisodesFor(artwork).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog(true)
            return
        }

        when (artwork) {
            is Movie -> {

                val movie = artwork.copy(
                    status = newStatus,
                    currentTime = 0L
                )
                _uiState.update { currentState ->
                    currentState.copy(selectedArtwork = movie)
                }

                // Save status in DB
                viewModelScope.launch { repository.saveMovie(movie) }

                Log.i("ArtworkViewModel", "${movie.title} is now ${movie.status}")

            }
            is Episode -> {

                val episode = artwork.copy(
                    status = newStatus,
                    currentTime = 0L
                )

                // Update list
                val episodes = _uiState.value.episodes.toMutableList()
                episodes.replaceAll { if (it.id == episode.id) episode else it }
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedArtwork = episode,
                        episodes = episodes
                    )
                }

                // Save status in DB
                viewModelScope.launch { repository.saveEpisode(episode) }

                Log.i("ArtworkViewModel", "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

            }
            else -> return
        }

    }

    fun changeWatchStatusForEpisodeAndPrevious() {

        val episode = uiState.value.selectedArtwork as? Episode ?: return
        val previousEpisodes = _uiState.value.episodes.getPreviousEpisodesFor(episode).filter { it.status != Status.WATCHED }

        val updatedEpisode = episode.copy(
            status = Status.WATCHED,
            currentTime = 0L
        )

        val updatedEpisodes = previousEpisodes.map {
            it.copy(
                status = Status.WATCHED,
                currentTime = 0L
            )
        } + updatedEpisode

        // Update list
        val episodes = _uiState.value.episodes.toMutableList()
        episodes.replaceAll { e ->
            updatedEpisodes.find { it.id == e.id } ?: e
        }
        _uiState.update { currentState ->
            currentState.copy(
                selectedArtwork = updatedEpisode,
                episodes = episodes,
                showStatusDialog = false
            )
        }

        // Save status in DB
        viewModelScope.launch { repository.saveEpisodes(episodes) }

    }

    fun saveTime(time: Long) = viewModelScope.launch {

        val artwork = uiState.value.selectedArtwork ?: return@launch

        uiState.value.let { state ->

            artwork.currentTime = time
            artwork.status = if (time.inMinutes >= artwork.duration * .9) Status.WATCHED else Status.IS_WATCHING

            when (state.selectedArtwork) {
                is Movie -> repository.saveMovie(state.selectedArtwork)
                is Episode -> repository.saveEpisode(state.selectedArtwork)
                else -> {}
            }

            dataStoreRepository.addWatchedArtwork(artworkId)

            Log.i("ArtworkViewModel", "${state.overview.title} saved at ${time.timeDescription}")

        }

    }

}