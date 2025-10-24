package com.kaem.flux.screens.media

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import com.kaem.flux.model.media.Status
import com.kaem.flux.utils.extensions.getPreviousEpisodesFor
import com.kaem.flux.utils.extensions.msToMin
import com.kaem.flux.utils.extensions.timeDescription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


data class MediaUiState(
    val overview: MediaOverview = MediaOverview(),
    val screen: ScreenState = ScreenState.LOADING,
    val selectedMedia: Media? = null,
    val episodes: List<Episode> = emptyList(),
    val currentSeason: Int = -1,
    val showPlayer: Boolean = false,
    val showStatusDialog: Boolean = false
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MediaRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<MediaEvent>()
    val event = _event.asSharedFlow()

    private val mediaId: Long = checkNotNull(savedStateHandle["mediaId"])

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    var backwardValue: Long = 10.seconds.inWholeMilliseconds
    var forwardValue: Long = 10.seconds.inWholeMilliseconds
    var subtitlesLanguage: Locale = Locale.getDefault()

    init {

        val (backward, forward) = dataStoreRepository.getPlayerButtonsValues()
        backwardValue = backward.seconds.inWholeMilliseconds
        forwardValue = forward.seconds.inWholeMilliseconds

        subtitlesLanguage = dataStoreRepository.getSubtitlesLanguage()

        getMedias(mediaId)
    }

    fun handleIntent(intent: MediaIntent) = viewModelScope.launch {
        when (intent) {
            MediaIntent.OnBackTap -> _event.emit(MediaEvent.BackToPreviousScreen)
            is MediaIntent.SelectEpisode -> selectMedia(intent.episode)
            is MediaIntent.SelectSeason -> selectSeason(intent.season)
            is MediaIntent.SaveTime -> saveTime(intent.time)
            MediaIntent.ShowPlayer -> showPlayer(true)
            MediaIntent.ClosePlayer -> showPlayer(false)
            else -> {}
        }
    }


    private fun getMedias(id: Long) = viewModelScope.launch {

        val (overview, movie, episodes) = repository.getMedia(id)

        _uiState.value = when {
            overview == null -> MediaUiState(screen = ScreenState.ERROR)
            movie != null -> MediaUiState(
                overview = overview,
                screen = ScreenState.CONTENT,
                selectedMedia = movie
            )

            !episodes.isNullOrEmpty() -> {
                
                val currentEpisode = episodes.lastOrNull { it.status == Status.IS_WATCHING }
                    ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
                    ?: episodes.first()

                MediaUiState(
                    overview = overview,
                    screen = ScreenState.CONTENT,
                    episodes = episodes,
                    currentSeason = currentEpisode.season,
                    selectedMedia = currentEpisode
                )
            }
            else -> MediaUiState(screen = ScreenState.ERROR)

        }

    }

    private fun selectMedia(media: Media?) {
        _uiState.update { currentState ->
            currentState.copy(selectedMedia = media)
        }
    }

    private fun selectSeason(season: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentSeason = season)
        }
    }

    private fun showPlayer(show: Boolean) {
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

        val media = uiState.value.selectedMedia ?: return

        val newStatus = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        if (
            checkPrevious
            && newStatus == Status.WATCHED
            && media is Episode
            && _uiState.value.episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog()
            return
        }

        when (media) {
            is Movie -> {

                val movie = media.copy(
                    status = newStatus,
                    currentTime = 0L
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedMedia = movie,
                        showStatusDialog = false
                    )
                }

                // Save status in DB
                viewModelScope.launch { repository.saveMovie(movie) }

                Log.i("MediaViewModel", "${movie.title} is now ${movie.status}")

            }
            is Episode -> {

                val episode = media.copy(
                    status = newStatus,
                    currentTime = 0L
                )

                // Update list
                val episodes = _uiState.value.episodes.toMutableList()
                episodes.replaceAll { if (it.id == episode.id) episode else it }
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedMedia = episode,
                        episodes = episodes,
                        showStatusDialog = false
                    )
                }

                // Save status in DB
                viewModelScope.launch { repository.saveEpisode(episode) }

                viewModelScope.launch { addOrRemoveToWatchedMedias() }

                Log.i("MediaViewModel", "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

            }
            else -> return
        }

    }

    fun changeWatchStatusForEpisodeAndPrevious() {

        val episode = uiState.value.selectedMedia as? Episode ?: return
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
                selectedMedia = updatedEpisode,
                episodes = episodes,
                showStatusDialog = false
            )
        }

        // Save status in DB
        viewModelScope.launch { repository.saveEpisodes(episodes) }

        viewModelScope.launch { addOrRemoveToWatchedMedias() }

    }

    private fun saveTime(time: Long) = viewModelScope.launch {

        val media = uiState.value.selectedMedia ?: return@launch
        val status = if (time.msToMin >= media.duration * .9) Status.WATCHED else Status.IS_WATCHING

        uiState.value.let { state ->

            media.currentTime = if (status == Status.WATCHED) 0L else time
            media.status = status

            when (media) {
                is Movie -> {

                    repository.saveMovie(media)

                    if (status == Status.WATCHED) dataStoreRepository.removeWatchedMedia(mediaId)
                    else dataStoreRepository.addWatchedMedia(mediaId)

                }
                is Episode -> {

                    repository.saveEpisode(media)

                    addOrRemoveToWatchedMedias()

                }
                else -> {}
            }

            Log.i("MediaViewModel", "${state.overview.title} saved at ${time.timeDescription()}")

        }

    }

    private suspend fun addOrRemoveToWatchedMedias() {
        if (uiState.value.episodes.all { it.status == Status.WATCHED }) dataStoreRepository.removeWatchedMedia(mediaId)
        else dataStoreRepository.addWatchedMedia(mediaId)
    }

}