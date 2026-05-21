package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.artwork.ArtworkEvent.OpenEpisodeInfo
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.utils.extensions.firstEpisode
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
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val progressUC: ProgressUC
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
        val showResetProgressDialog: Boolean = false
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
        artworkUC.flow,
        settingsRepository.flow,
        _subState
    ) { artworkContent, settings, subState ->
        buildUiState(
            artworkState = artworkContent,
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
        artworkUC.searchArtwork(artworkId = artworkId)
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
            ArtworkIntent.OpenArtworkInfo -> openArtworkInfo()
            is ArtworkIntent.OpenEpisodeInfo -> _event.emit(OpenEpisodeInfo(episode = intent.episode))
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
            is ArtworkIntent.ShowResetProgressDialog -> showResetProgressDialog(show = intent.show)
            ArtworkIntent.ResetProgress -> resetProgress()
        }
    }

    //endregion

    //region Private Methods

    private fun buildUiState(
        artworkState: ArtworkUC.State,
        settings: SettingsRepository.State,
        subState: UserState
    ) : ArtworkUiState {

        var artwork: Artwork? = null
        var movie: Movie? = null
        var episodes: List<Episode> = emptyList()

        when (artworkState) {
            ArtworkUC.State.ERROR -> {}
            is ArtworkUC.State.MOVIE -> {
                artwork = artworkState.artwork
                movie = artworkState.movie
            }
            is ArtworkUC.State.SHOW -> {
                artwork = artworkState.artwork
                episodes = artworkState.episodes
            }
        }


        val episode = episodes.firstOrNull { it.id == (subState.selectedMedia as? Episode)?.id } // Selected media by user
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
                    seasons = (artworkState as? ArtworkUC.State.SHOW)?.seasons ?: emptyList(),
                    episodes = episodes,
                    season = season,
                    media = media,
                    episodePendingConfirmation = subState.episodePendingConfirmation,
                    useExternalPlayer = settings.externalPlayer,
                    showResetProgressDialog = subState.showResetProgressDialog
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

    private suspend fun openArtworkInfo() {
        uiState.value.artwork.let { artwork ->
            _event.emit(ArtworkEvent.OpenArtworkInfo(artwork = artwork))
        }
    }

    private suspend fun changeWatchStatus(media: Media) {

        val status = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        progressUC.changeMediaStatus(
            media = media,
            status = status
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

            progressUC.markPreviousEpisodesAsWatchedFor(episode = episode)

            state.copy(episodePendingConfirmation = null)

        }

    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        selectedMedia?.let { media ->
            progressUC.saveProgress(media = media, progress = progress)
            selectedMedia = null
        }
    }

    private fun showResetProgressDialog(show: Boolean) {
        _subState.update { it.copy(showResetProgressDialog = show) }
    }

    private suspend fun resetProgress() {

        val currentState = uiState.value

        progressUC.resetProgress(artwork = currentState.artwork)

        _subState.update {

            it.copy(
                selectedMedia = currentState.media as? Movie ?: currentState.episodes.firstEpisode,
                showResetProgressDialog = false
            )

        }

    }

    //endregion

}