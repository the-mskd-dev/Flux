package com.kaem.flux.screens.media

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import com.kaem.flux.model.media.Status
import com.kaem.flux.utils.extensions.getPreviousEpisodesFor
import com.kaem.flux.utils.extensions.msToMin
import com.kaem.flux.utils.extensions.timeDescription
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.Boolean
import kotlin.time.Duration.Companion.minutes

@HiltViewModel(assistedFactory = MediaViewModel.Factory::class)
class MediaViewModel @AssistedInject constructor(
    @Assisted val mediaId: Long,
    private val repository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    //region Hilt

    @AssistedFactory
    interface Factory {
        fun create(mediaId: Long): MediaViewModel
    }

    //endregion

    //region sub states

    @Immutable
    private data class MediaSubState(
        val screen: ScreenState = ScreenState.LOADING,
        val overview: MediaOverview = MediaOverview(),
        val media: Media = MediaMockups.episode1,
        val episodes: List<Episode> = emptyList(),
        val season: Int = -1,
        val showPlayer: Boolean = false,
        val showStatusDialog: Boolean = false
    )

    private var episodeTmp: Episode? = null

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<MediaEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    private val _subState = MutableStateFlow(MediaSubState())

    val uiState: StateFlow<MediaUiState> = combine(
        _subState,
        settingsRepository.settingsPreferencesFlow
    ) { subState, settings ->
        MediaUiState(
            screen = subState.screen,
            overview = subState.overview,
            episodes = subState.episodes,
            season = subState.season,
            media = subState.media,
            showPlayer = subState.showPlayer,
            showStatusDialog = subState.showStatusDialog,
            playerBackward = settings.playerBackwardValue.minutes.inWholeMilliseconds,
            playerForward = settings.playerForwardValue.minutes.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MediaUiState()
    )


    //endregion

    //region Init

    init { getMedias(mediaId) }

    //endregion

    //region Public Methods

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

    //endregion

    //region Private Methods

    private fun getMedias(id: Long) = viewModelScope.launch {

        val (overview, movie, episodes) = repository.getMedia(id)

        val media = movie ?: episodes.firstOrNull { it.status == Status.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
            ?: episodes.firstOrNull()

        val subState = when {
            overview == null || media == null -> MediaSubState(screen = ScreenState.ERROR)
            else -> {

                MediaSubState(
                    overview = overview,
                    screen = ScreenState.CONTENT,
                    episodes = episodes,
                    season = (media as? Episode)?.season ?: -1,
                    media = media,
                )

            }
        }

        _subState.emit(subState)
    }

    private fun selectSeason(season: Int) {
        _subState.update { currentState ->
            currentState.copy(season = season)
        }
    }

    private fun playMedia(media: Media) {
        _subState.update { currentState ->
            currentState.copy(
                media = media,
                showPlayer = true
            )
        }
    }

    private fun closePlayer() {
        _subState.update { currentState ->
            currentState.copy(showPlayer = false)
        }
    }

    private fun showStatusDialog() {
        _subState.update { currentState ->
            currentState.copy(showStatusDialog = true)
        }
    }

    private fun closeStatusDialog() {
        _subState.update { currentState ->
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
            && _subState.value.episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
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
        _subState.update { currentState ->
            currentState.copy(
                media = movieUpdated,
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

        _subState.update { currentState ->
            currentState.copy(
                media = if ((currentState.media as? Episode)?.id == episode.id) updatedEpisode else currentState.media, // Update media only if it's the same as selected
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
        _subState.update { currentState ->
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

        val currentState = uiState.value
        val currentMedia = currentState.media

        val newStatus = if (time.msToMin >= currentMedia.duration * .9) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else time

        val updatedMedia = when (currentMedia) {
            is Movie -> currentMedia.copy(currentTime = newTime, status = newStatus)
            is Episode -> currentMedia.copy(currentTime = newTime, status = newStatus)
            else -> currentMedia
        }

        _subState.update { state ->

            val updatedEpisodes = if (updatedMedia is Episode) {
                state.episodes.map { if (it.id == updatedMedia.id) updatedMedia else it }
            } else state.episodes

            state.copy(
                media = updatedMedia,
                episodes = updatedEpisodes
            )
        }

        when (updatedMedia) {
            is Movie -> {
                if (newStatus == Status.WATCHED) userRepository.removeWatchedMedia(mediaId)
                else userRepository.addWatchedMedia(mediaId)
                repository.saveMovie(updatedMedia)
            }
            is Episode -> {
                repository.saveEpisode(updatedMedia)
                addOrRemoveToWatchedMedias()
            }
        }

        Log.i("MediaViewModel", "${updatedMedia.title} saved at ${time.timeDescription()}")

    }

    private suspend fun addOrRemoveToWatchedMedias() {
        if (uiState.value.episodes.all { it.status == Status.WATCHED }) userRepository.removeWatchedMedia(mediaId)
        else userRepository.addWatchedMedia(mediaId)
    }

    //endregion

}