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
import com.kaem.flux.utils.extensions.msToMin
import com.kaem.flux.utils.extensions.timeDescription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


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

    var backwardValue: Long = 10.seconds.inWholeMilliseconds
    var forwardValue: Long = 10.seconds.inWholeMilliseconds
    var subtitlesLanguage: Locale = Locale.getDefault()

    init {

        val (backward, forward) = dataStoreRepository.getPlayerButtonsValues()
        backwardValue = backward.seconds.inWholeMilliseconds
        forwardValue = forward.seconds.inWholeMilliseconds

        subtitlesLanguage = dataStoreRepository.getSubtitlesLanguage()

        getArtworks(artworkId)
    }

    private fun getArtworks(id: Long) = viewModelScope.launch {

        val (overview, movie, episodes) = repository.getArtwork(id)

        _uiState.value = when {
            overview == null -> ArtworkUiState(screen = ScreenState.ERROR)
            movie != null -> ArtworkUiState(
                overview = overview,
                screen = ScreenState.CONTENT,
                selectedArtwork = movie
            )

            !episodes.isNullOrEmpty() -> {
                
                val currentEpisode = episodes.lastOrNull { it.status == Status.IS_WATCHING }
                    ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
                    ?: episodes.first()

                ArtworkUiState(
                    overview = overview,
                    screen = ScreenState.CONTENT,
                    episodes = episodes,
                    currentSeason = currentEpisode.season,
                    selectedArtwork = currentEpisode
                )
            }
            else -> ArtworkUiState(screen = ScreenState.ERROR)

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

    private fun showStatusDialog() {
        _uiState.update { currentState ->
            currentState.copy(showStatusDialog = true)
        }
    }

    fun changeWatchStatus(checkPrevious: Boolean = true) {

        val artwork = uiState.value.selectedArtwork ?: return

        val newStatus = if (artwork.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        if (
            checkPrevious
            && newStatus == Status.WATCHED
            && artwork is Episode
            && _uiState.value.episodes.getPreviousEpisodesFor(artwork).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog()
            return
        }

        when (artwork) {
            is Movie -> {

                val movie = artwork.copy(
                    status = newStatus,
                    currentTime = 0L
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedArtwork = movie,
                        showStatusDialog = false
                    )
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
                        episodes = episodes,
                        showStatusDialog = false
                    )
                }

                // Save status in DB
                viewModelScope.launch { repository.saveEpisode(episode) }

                viewModelScope.launch { addOrRemoveToWatchedArtworks() }

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

        viewModelScope.launch { addOrRemoveToWatchedArtworks() }

    }

    fun saveTime(time: Long) = viewModelScope.launch {

        val artwork = uiState.value.selectedArtwork ?: return@launch
        val status = if (time.msToMin >= artwork.duration * .9) Status.WATCHED else Status.IS_WATCHING

        uiState.value.let { state ->

            artwork.currentTime = if (status == Status.WATCHED) 0L else time
            artwork.status = status

            when (artwork) {
                is Movie -> {

                    repository.saveMovie(artwork)

                    if (status == Status.WATCHED) dataStoreRepository.removeWatchedArtwork(artworkId)
                    else dataStoreRepository.addWatchedArtwork(artworkId)

                }
                is Episode -> {

                    repository.saveEpisode(artwork)
                    val episodes = state.episodes.sortedWith(
                        compareBy<Episode> { it.season }.thenBy { it.number }
                    )

                    addOrRemoveToWatchedArtworks()

                }
                else -> {}
            }

            Log.i("ArtworkViewModel", "${state.overview.title} saved at ${time.timeDescription()}")

        }

    }

    private suspend fun addOrRemoveToWatchedArtworks() {
        if (uiState.value.episodes.all { it.status == Status.WATCHED }) dataStoreRepository.removeWatchedArtwork(artworkId)
        else dataStoreRepository.addWatchedArtwork(artworkId)
    }

}