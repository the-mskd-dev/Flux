package com.kaem.flux.screens.media

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.data.repository.SettingsPreferences
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
        val selectedMedia: Media? = null,
        val selectedSeason: Int? = null,
        val showPlayer: Boolean = false,
        val episodePendingConfirmation: Episode? = null,
    )

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<MediaEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    private val _subState = MutableStateFlow(MediaSubState())

    val uiState: StateFlow<MediaUiState> = combine(
        repository.getMediaFlow(mediaId = mediaId),
        _subState,
        settingsRepository.flow
    ) { mediaContent, subState, settings ->

        buildUiState(
            mediaContent = mediaContent,
            subState = subState,
            settings = settings
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MediaUiState()
    )


    //endregion

    //region Public Methods

    fun handleIntent(intent: MediaIntent) = viewModelScope.launch {
        when (intent) {
            MediaIntent.OnBackTap -> _event.emit(MediaEvent.BackToPreviousScreen)
            is MediaIntent.SelectSeason -> selectSeason(season = intent.season)
            is MediaIntent.SaveWatchTime -> saveWatchTime(media = intent.media, time = intent.time)
            is MediaIntent.PlayMedia -> playMedia(media = intent.media)
            MediaIntent.CloseEpisodesStatusDialog -> closeStatusDialog()
            MediaIntent.ClosePlayer -> closePlayer()
            is MediaIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            MediaIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
        }
    }

    //endregion

    //region Private Methods

    private fun buildUiState(mediaContent: MediaRepository.Content, subState: MediaSubState, settings: SettingsPreferences) : MediaUiState {

        val overview = mediaContent.mediaOverview
        val movie = mediaContent.movie
        val episodes = mediaContent.episodes

        val nextEpisode = episodes.firstOrNull { it.status == Status.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
            ?: episodes.firstOrNull()

        val media = when (subState.selectedMedia) {
            is Movie -> movie
            is Episode -> {

                if (subState.selectedMedia.id == nextEpisode?.id)
                    nextEpisode
                else
                    subState.selectedMedia

            }
            null -> movie ?: nextEpisode
            else -> subState.selectedMedia
        }

        val season = subState.selectedSeason ?: (media as? Episode)?.season ?: -1

        return when {
            overview == null || media == null -> MediaUiState(screen = ScreenState.ERROR)
            else -> {

                MediaUiState(
                    screen = ScreenState.CONTENT,
                    overview = overview,
                    episodes = episodes,
                    season = season,
                    media = media,
                    showPlayer = subState.showPlayer,
                    episodePendingConfirmation = subState.episodePendingConfirmation,
                    playerBackward = settings.playerBackwardValue.seconds.inWholeMilliseconds,
                    playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
                    subtitlesLanguage = settings.subtitlesLanguage
                )

            }
        }

    }

    private fun selectSeason(season: Int) {
        _subState.update { currentState ->
            currentState.copy(selectedSeason = season)
        }
    }

    private fun playMedia(media: Media) {
        _subState.update { currentState ->
            currentState.copy(
                selectedMedia = media,
                showPlayer = true
            )
        }
    }

    private fun closePlayer() {
        _subState.update { currentState ->
            currentState.copy(showPlayer = false)
        }
    }

    private fun showStatusDialog(episode: Episode) {
        _subState.update { currentState ->
            currentState.copy(episodePendingConfirmation = episode)
        }
    }

    private fun closeStatusDialog() {
        _subState.update { currentState ->
            currentState.copy(episodePendingConfirmation = null)
        }
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
            && uiState.value.episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog(episode = media)
        }

    }

    private suspend fun changeMovieStatus(movie: Movie, status: Status) {

        val movieUpdated = movie.copy(
            status = status,
            currentTime = 0L
        )

        repository.saveMovie(movieUpdated) // Save status in DB

        Log.i("MediaViewModel", "${movie.title} is now ${movie.status}")

    }

    private suspend fun changeEpisodeStatus(episode: Episode, status: Status) {

        val updatedEpisode = episode.copy(
            status = status,
            currentTime = 0L
        )

        repository.saveEpisodes(listOf(updatedEpisode)) // Save status in DB
        addOrRemoveToWatchedMedias()

        Log.i("MediaViewModel", "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

    }

    private suspend fun markPreviousEpisodesAsWatched() {

        var episodesToSave: List<Episode> = emptyList()

        _subState.update { state ->

            val episode = state.episodePendingConfirmation ?: return
            val previousEpisodes = uiState.value.episodes.getPreviousEpisodesFor(episode).filter { it.status != Status.WATCHED }

            if (previousEpisodes.isEmpty())
                return@update state.copy(episodePendingConfirmation = null)

            episodesToSave = previousEpisodes.map {
                it.copy(
                    status = Status.WATCHED,
                    currentTime = 0L
                )
            }

            state.copy(episodePendingConfirmation = null)

        }

        repository.saveEpisodes(episodesToSave) // Save status in DB

        Log.i("MediaViewModel", "${episodesToSave.size} episodes marked as watched")
    }

    private suspend fun saveWatchTime(media: Media, time: Long) {

        val newStatus = if (time.msToMin >= media.duration * .9) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else time

        val updatedMedia = when (media) {
            is Movie -> media.copy(currentTime = newTime, status = newStatus)
            is Episode -> media.copy(currentTime = newTime, status = newStatus)
            else -> media
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