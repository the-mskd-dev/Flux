package com.mskd.flux.screens.artwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.artwork.ArtworkEvent.OpenUrlInfo
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

    //region Variables

    private val artworkContent: ArtworkContent? get() = (uiState.value.state as? State.Content)?.content
    private val fullArtwork: FullArtwork? get() = artworkContent?.fullArtwork
    private val episodes: List<Episode> get() = (fullArtwork as? FullArtwork.FullShow)?.episodes.orEmpty()

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow()

    private val _userState = MutableStateFlow(ArtworkUserState())

    val uiState: StateFlow<ArtworkUiState> = combine(
        artworkUC.flow,
        settingsRepository.flow,
        _userState,
    ) { artworkState, settings, userState ->

        when (artworkState) {
            is State.Loading -> ArtworkUiState(state = State.Loading)
            is State.Error -> ArtworkUiState(state = State.Error)
            is State.Content -> {

                val dataState = ArtworkDataState(
                    fullArtwork = artworkState.content,
                    useExternalPlayer = settings.externalPlayer,
                )

                ArtworkUiState(state = mergeStates(dataState, userState))

            }
        }

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
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            ArtworkIntent.OpenArtworkInfo -> openArtworkInfo()
            is ArtworkIntent.OpenEpisodeInfo -> _event.emit(OpenUrlInfo(url = intent.episode.infoUrl))
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
            ArtworkIntent.ShowResetProgressDialog -> showResetProgressDialog()
            ArtworkIntent.ResetProgress -> resetProgress()
            ArtworkIntent.CloseDialog -> closeDialog()
        }
    }

    //endregion

    //region Private Methods

    private fun mergeStates(
        dataState: ArtworkDataState,
        userState: ArtworkUserState,
    ): State<ArtworkContent> {

        val selectedMedia = resolveSelectedMedia(
            fullArtwork = dataState.fullArtwork,
            userState = userState
        ) ?: return State.Error

        return State.Content(
            ArtworkContent(
                fullArtwork = dataState.fullArtwork,
                selectedMedia = selectedMedia,
                selectedSeason = season,
                useExternalPlayer = dataState.useExternalPlayer,
                dialog = userState.dialog,
            )
        )
    }

    private fun resolveSelectedMedia(
        fullArtwork: FullArtwork,
        userState: ArtworkUserState,
    ): Media? {
        return when (fullArtwork) {
            is FullArtwork.FullMovie -> fullArtwork.movie
            is FullArtwork.FullShow -> {
                val episodes = fullArtwork.episodes.filter { it.season == season }
                episodes
                    .firstOrNull { it.id == userState.selectedMedia?.mediaId }
                    ?: episodes.firstEpisodeToWatch
            }
        }
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        _userState.update { it.copy(selectedMedia = media) }

        if (artworkContent?.useExternalPlayer == true && !forceInternal)
            _event.emit(ArtworkEvent.LaunchExternalPlayer(media = media))
        else
            _event.emit(ArtworkEvent.PlayMedia(mediaId = media.mediaId))

    }

    private fun showStatusDialog(episode: Episode) {
        _userState.update { it.copy(dialog = ArtworkDialog.EpisodeStatusConfirmation(episode = episode)) }
    }

    private suspend fun openArtworkInfo() {
        artworkContent?.let { content ->

            val url = when (val fullArtwork = content.fullArtwork) {
                is FullArtwork.FullMovie -> fullArtwork.artwork.infoUrl
                is FullArtwork.FullShow -> fullArtwork.seasons.find { it.season == season }?.infoUrl ?: return@let
            }

            _event.emit(ArtworkEvent.OpenUrlInfo(url = url))
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

        _userState.update { state ->

            val episode = (state.dialog as? ArtworkDialog.EpisodeStatusConfirmation)?.episode ?: return

            progressUC.markPreviousEpisodesAsWatchedFor(episode = episode)

            state.copy(dialog = null)

        }

    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        artworkContent?.selectedMedia?.let { media ->
            progressUC.saveProgress(media = media, progress = progress)
        }
    }

    private fun showResetProgressDialog() {
        _userState.update { it.copy(dialog = ArtworkDialog.ResetProgressConfirmation) }
    }

    private suspend fun resetProgress() {

        val fullArtwork = fullArtwork ?: return
        val selectedSeason = artworkContent?.selectedSeason

        progressUC.resetProgress(artwork = fullArtwork.artwork, season = selectedSeason)

        _userState.update { state ->

            state.copy(
                selectedMedia = (fullArtwork as? FullArtwork.FullMovie)?.movie ?: episodes.filter { it.season == selectedSeason }.firstEpisode,
                dialog = null
            )

        }

    }

    private suspend fun closeDialog() {
        _userState.update { it.copy(dialog = null) }
    }

    //endregion

}