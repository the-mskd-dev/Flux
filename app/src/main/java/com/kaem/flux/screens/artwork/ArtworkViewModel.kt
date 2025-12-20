package com.kaem.flux.screens.artwork

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.SettingsPreferences
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
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
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = ArtworkViewModel.Factory::class)
class ArtworkViewModel @AssistedInject constructor(
    @Assisted val mediaId: Long,
    private val repository: ArtworkRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    //region Hilt

    @AssistedFactory
    interface Factory {
        fun create(mediaId: Long): ArtworkViewModel
    }

    //endregion

    //region sub states

    @Immutable
    private data class UserState(
        val selectedMedia: Media? = null,
        val selectedSeason: Int? = null,
        val episodePendingConfirmation: Episode? = null,
    )

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    private val _subState = MutableStateFlow(UserState())

    val uiState: StateFlow<ArtworkUiState> = combine(
        repository.flow,
        _subState
    ) { mediaContent, subState ->
        buildUiState(
            mediaContent = mediaContent,
            subState = subState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArtworkUiState()
    )


    //endregion

    //regin Init

    init {
        repository.searchArtwork(mediaId = mediaId)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: ArtworkIntent) = viewModelScope.launch {
        when (intent) {
            ArtworkIntent.OnBackTap -> _event.emit(ArtworkEvent.BackToPreviousScreen)
            is ArtworkIntent.SelectSeason -> selectSeason(season = intent.season)
            is ArtworkIntent.PlayMedia -> playMedia(media = intent.media)
            ArtworkIntent.CloseEpisodesStatusDialog -> closeStatusDialog()
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
        }
    }

    //endregion

    //region Private Methods

    private fun buildUiState(mediaContent: ArtworkRepository.Content, subState: UserState) : ArtworkUiState {

        val artwork = mediaContent.artwork
        val movie = mediaContent.movie
        val episodes = mediaContent.episodes

        val episode = episodes.firstOrNull { it.id == (subState.selectedMedia as? Episode)?.id } // Selected media by user
            ?: episodes.firstOrNull { it.id == (uiState.value.media as? Episode)?.id } // Current selected media
            ?: episodes.firstOrNull { it.status == Status.IS_WATCHING } // First episode watching
            ?: episodes.firstOrNull { it.status == Status.TO_WATCH } // First episode to watch
            ?: episodes.firstOrNull() // First episode

        val media = movie ?: episode

        val season = subState.selectedSeason ?: (media as? Episode)?.season ?: -1

        return when {
            artwork == null || media == null -> ArtworkUiState(screen = ScreenState.ERROR)
            else -> {

                ArtworkUiState(
                    screen = ScreenState.CONTENT,
                    artwork = artwork,
                    episodes = episodes,
                    season = season,
                    media = media,
                    episodePendingConfirmation = subState.episodePendingConfirmation,
                )

            }
        }

    }

    private fun selectSeason(season: Int) {
        _subState.update { it.copy(selectedSeason = season) }
    }

    private suspend fun playMedia(media: Media) {
        _subState.update { it.copy(selectedMedia = media) }
        _event.emit(ArtworkEvent.PlayMedia(media = media))
    }

    private fun showStatusDialog(episode: Episode) {
        _subState.update { it.copy(episodePendingConfirmation = episode) }
    }

    private fun closeStatusDialog() {
        _subState.update { it.copy(episodePendingConfirmation = null) }
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

    private suspend fun addOrRemoveToWatchedMedias() {
        if (uiState.first().episodes.all { it.status == Status.WATCHED }) userRepository.removeWatchedMedia(mediaId)
        else userRepository.addWatchedMedia(mediaId)
    }

    //endregion

}