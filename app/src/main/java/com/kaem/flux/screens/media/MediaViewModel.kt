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

    private var episodeTmp: Episode? = null

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
            is MediaIntent.SelectSeason -> selectSeason(season = intent.season)
            is MediaIntent.SaveWatchTime -> saveWatchTime(time = intent.time)
            is MediaIntent.PlayMedia -> playMedia(media = intent.media)
            MediaIntent.CloseEpisodesStatusDialog -> closeStatusDialog()
            MediaIntent.ClosePlayer -> closePlayer()
            is MediaIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            MediaIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
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
                    selectedMedia = currentEpisode,
                )
            }
            else -> MediaUiState(screen = ScreenState.ERROR)

        }

    }

    private fun selectSeason(season: Int) {
        _uiState.update { currentState ->
            currentState.copy(currentSeason = season)
        }
    }

    private fun playMedia(media: Media) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedMedia = media,
                showPlayer = true
            )
        }
    }

    private fun closePlayer() {
        _uiState.update { currentState ->
            currentState.copy(showPlayer = false)
        }
    }

    private fun showStatusDialog() {
        _uiState.update { currentState ->
            currentState.copy(showStatusDialog = true)
        }
    }

    private fun closeStatusDialog() {
        _uiState.update { currentState ->
            currentState.copy(showStatusDialog = false)
        }
        episodeTmp = null
    }

    private suspend fun changeWatchStatus(media: Media) {

        val newStatus = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        when (media) {
            is Movie -> changeMovieStatus(movie = media, status = newStatus)
            is Episode -> changeEpisodeStatus(episode = media, status = newStatus)
        }

        if (
            newStatus == Status.WATCHED
            && media is Episode
            && _uiState.value.episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            episodeTmp = media
            showStatusDialog()
        }

    }

    private suspend fun changeMovieStatus(movie: Movie, status: Status) {

        val movieUpdated = movie.copy(
            status = status,
            currentTime = 0L
        )
        _uiState.update { currentState ->
            currentState.copy(
                selectedMedia = movieUpdated,
                showStatusDialog = false
            )
        }

        // Save status in DB
        repository.saveMovie(movieUpdated)

        Log.i("MediaViewModel", "${movie.title} is now ${movie.status}")

    }

    private suspend fun changeEpisodeStatus(episode: Episode, status: Status) {

        val state = uiState.value

        val updatedEpisode = episode.copy(
            status = status,
            currentTime = 0L
        )

        // Update list
        val episodes = state.episodes.toMutableList()
        episodes.replaceAll { e ->
            if (e.id == episode.id) updatedEpisode else e
        }

        _uiState.update { currentState ->
            currentState.copy(
                selectedMedia = if ((currentState.selectedMedia as? Episode)?.id == episode.id) updatedEpisode else currentState.selectedMedia, // Update media only if it's the same as selected
                episodes = episodes,
                showStatusDialog = false
            )
        }

        // Save status in DB
        repository.saveEpisodes(listOf(updatedEpisode))

        addOrRemoveToWatchedMedias()

        Log.i("MediaViewModel", "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

    }

    private suspend fun markPreviousEpisodesAsWatched() {

        val state = uiState.value
        val episode = episodeTmp ?: return
        val previousEpisodes = state.episodes.getPreviousEpisodesFor(episode).filter { it.status != Status.WATCHED }

        val updatedEpisodes = previousEpisodes.map {
            it.copy(
                status = Status.WATCHED,
                currentTime = 0L
            )
        }

        // Update list
        val episodes = state.episodes.toMutableList()
        episodes.replaceAll { e ->
            updatedEpisodes.find { it.id == e.id } ?: e
        }
        _uiState.update { currentState ->
            currentState.copy(
                episodes = episodes,
                showStatusDialog = false
            )
        }

        // Save status in DB
        repository.saveEpisodes(episodes)

        episodeTmp = null

        Log.i("MediaViewModel", "Episodes previous season ${episode.season} episode ${episode.number} are marked as watched")

    }

    private suspend fun saveWatchTime(time: Long) {

        val media = uiState.value.selectedMedia ?: return
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

                    _uiState.update { currentState ->
                        val episodes = currentState.episodes.toMutableList()
                        episodes.replaceAll { episode ->
                            if (episode.id == media.id) media else episode
                        }
                        currentState.copy(
                            episodes = episodes
                        )
                    }

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