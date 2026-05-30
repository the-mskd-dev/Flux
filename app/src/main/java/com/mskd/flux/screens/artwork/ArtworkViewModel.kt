package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.artwork.ArtworkEvent.OpenEpisodeInfo
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.utils.extensions.firstEpisode
import com.mskd.flux.utils.extensions.firstEpisodeToWatch
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
    @Assisted val season: Int?,
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val progressUC: ProgressUC
) : ViewModel() {

    //region Hilt

    @AssistedFactory
    interface Factory {
        fun create(artworkId: Long, season: Int?): ArtworkViewModel
    }

    //endregion

    //region Sub states

    @Immutable
    private data class UserState(
        val selectedMedia: Media? = null,
        val dialog: ArtworkDialog? = null
    )

    //endregion

    //region Variables

    private var selectedMedia: Media? = null

    private val fullArtwork : FullArtwork? get() = (uiState.value.state as? State.Content)?.content
    private val episodes : List<Episode> get() = (fullArtwork as? FullArtwork.FullShow)?.episodes.orEmpty()

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow()

    private val _subState = MutableStateFlow(UserState())

    val uiState: StateFlow<ArtworkUiState> = combine(
        artworkUC.flow,
        settingsRepository.flow,
        _subState,
    ) { artworkState, settings, subState ->
        buildUiState(
            artworkState = artworkState,
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
        artworkState: State<FullArtwork>,
        settings: SettingsRepository.State,
        subState: UserState,
    ) : ArtworkUiState {

        val fullArtwork = (artworkState as? State.Content<FullArtwork>)?.content
        val fullMovie = fullArtwork as? FullArtwork.FullMovie
        val fullShow = (fullArtwork as? FullArtwork.FullShow)?.let {
            it.copy(
                episodes = it.episodes.filter { e -> e.season == (season ?: -1) }
            )
        }

        val episodes: List<Episode> = fullShow?.episodes ?: emptyList()
        val episode = episodes.firstOrNull { it.id == (subState.selectedMedia as? Episode)?.id } // Selected media by user
            ?: episodes.firstEpisodeToWatch

        val media = fullMovie?.movie ?: episode

        return when {
            media == null -> ArtworkUiState(state = State.Error)
            else -> {
                ArtworkUiState(
                    state = (fullShow ?: fullMovie)?.let { State.Content(it) } ?: State.Error,
                    selectedMedia = media,
                    useExternalPlayer = settings.externalPlayer,
                    dialog = subState.dialog
                )
            }
        }

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
        _subState.update { it.copy(dialog = ArtworkDialog.EpisodeStatusConfirmation(episode = episode)) }
    }

    private fun closeStatusDialog() {
        _subState.update { it.copy(dialog = null) }
    }

    private suspend fun openArtworkInfo() {
        (uiState.value.state as? State.Content)?.content?.let {
            _event.emit(ArtworkEvent.OpenArtworkInfo(artwork = it.artwork))
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
            && episodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            showStatusDialog(episode = media)
        }

    }

    private suspend fun markPreviousEpisodesAsWatched() {

        _subState.update { state ->

            val episode = (state.dialog as? ArtworkDialog.EpisodeStatusConfirmation)?.episode ?: return

            progressUC.markPreviousEpisodesAsWatchedFor(episode = episode)

            state.copy(dialog = null)

        }

    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        selectedMedia?.let { media ->
            progressUC.saveProgress(media = media, progress = progress)
            selectedMedia = null
        }
    }

    private fun showResetProgressDialog(show: Boolean) {
        _subState.update { it.copy(dialog = if (show) ArtworkDialog.ResetProgressConfirmation else null) }
    }

    private suspend fun resetProgress() {

        val fullArtwork = fullArtwork ?: return

        progressUC.resetProgress(artwork = fullArtwork.artwork)

        _subState.update {

            it.copy(
                selectedMedia = (fullArtwork as? FullArtwork.FullMovie)?.movie ?: episodes.firstEpisode,
                dialog = null
            )

        }

    }

    //endregion

}