package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.useCases.mediaProgress.MediaProgressUC
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ArtworkViewModel.Factory::class)
class ArtworkViewModel @AssistedInject constructor(
    @Assisted val artworkId: Long,
    private val repository: ArtworkRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaProgressUC: MediaProgressUC
) : ViewModel() {

    //region Hilt

    @AssistedFactory
    interface Factory {
        fun create(artworkId: Long): ArtworkViewModel
    }

    //endregion

    //region Sub states

    @Immutable
    private data class UserState(
        val selectedMedia: Media? = null,
        val selectedSeason: Int? = null,
        val episodePendingConfirmation: Episode? = null,
    )

    //endregion

    //region Variables

    private var selectedMedia: Media? = null

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow()

    private val _subState = MutableStateFlow(UserState())

    val uiState: StateFlow<ArtworkUiState> = combine(
        repository.flow,
        settingsRepository.flow,
        _subState
    ) { mediaContent, settings, subState ->
        buildUiState(
            mediaState = mediaContent,
            settings = settings,
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
        repository.searchArtwork(artworkId = artworkId)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: ArtworkIntent) = viewModelScope.launch {
        when (intent) {
            ArtworkIntent.OnBackTap -> _event.emit(ArtworkEvent.BackToPreviousScreen)
            is ArtworkIntent.SelectSeason -> selectSeason(season = intent.season)
            is ArtworkIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            ArtworkIntent.CloseEpisodesStatusDialog -> closeStatusDialog()
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            is ArtworkIntent.OpenArtworkInfo -> _event.emit(ArtworkEvent.OpenArtworkInfo(artwork = intent.artwork))
            is ArtworkIntent.OpenEpisodeInfo -> _event.emit(ArtworkEvent.OpenEpisodeInfo(episode = intent.episode))
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
        }
    }

    //endregion

    //region Private Methods

    private fun buildUiState(
        mediaState: ArtworkRepository.State,
        settings: SettingsRepository.State,
        subState: UserState
    ) : ArtworkUiState {

        val artwork = mediaState.artwork
        val movie = mediaState.movie
        val episodes = mediaState.episodes

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
                    useExternalPlayer = settings.externalPlayer
                )

            }
        }

    }

    private fun selectSeason(season: Int) {
        _subState.update { it.copy(selectedSeason = season) }
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        _subState.update { it.copy(selectedMedia = media) }
        selectedMedia = media

        if (uiState.value.useExternalPlayer && !forceInternal)
            _event.emit(ArtworkEvent.LaunchExternalPlayer(media = media))
        else
            _event.emit(ArtworkEvent.PlayMedia(mediaId = media.mediaId))

    }

    private fun showStatusDialog(episode: Episode) {
        _subState.update { it.copy(episodePendingConfirmation = episode) }
    }

    private fun closeStatusDialog() {
        _subState.update { it.copy(episodePendingConfirmation = null) }
    }

    private suspend fun changeWatchStatus(media: Media) {

        val status = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        mediaProgressUC.changeMediaStatus(
            media = media,
            status =status
        )

        if (
            status == Status.WATCHED
            && media is Episode
            && uiState.value.episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog(episode = media)
        }

    }

    private suspend fun markPreviousEpisodesAsWatched() {

        _subState.update { state ->

            val episode = state.episodePendingConfirmation ?: return

            mediaProgressUC.markPreviousEpisodesAsWatchedFor(episode = episode)

            state.copy(episodePendingConfirmation = null)

        }

    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        selectedMedia?.let { media ->
            mediaProgressUC.saveProgress(media = media, progress = progress)
            selectedMedia = null
        }
    }

    //endregion

}