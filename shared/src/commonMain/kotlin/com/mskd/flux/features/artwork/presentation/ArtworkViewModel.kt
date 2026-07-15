package com.mskd.flux.features.artwork.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.presentation.ArtworkEvent.OpenUrlInfo
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.extensions.firstEpisode
import com.mskd.flux.utils.extensions.firstEpisodeToWatch
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtworkViewModel(
    private val artworkId: Long,
    private val season: Int?,
    settingsDataStore: SettingsDataStore,
    observeArtworkUseCase: ObserveArtworkUseCase,
    private val changeMediaStatus: ChangeMediaStatusUseCase,
    private val markPreviousAsWatched: MarkPreviousAsWatchedUseCase,
    private val resetProgress: ResetProgressUseCase,
    private val saveProgress: SaveProgressUseCase,
) : ViewModel() {

    //region Computed properties

    private val artworkContent: ArtworkContent? get() = (uiState.value.state as? State.Content)?.content
    private val fullArtwork: FullArtwork? get() = artworkContent?.fullArtwork
    private val episodes: List<Episode> get() = (fullArtwork as? FullArtwork.FullShow)?.episodes.orEmpty()

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow()

    private val _userState = MutableStateFlow(ArtworkUserState())

    val uiState: StateFlow<ArtworkUiState> = combine(
        observeArtworkUseCase.flow,
        settingsDataStore.flow,
        _userState,
    ) { artworkState, settings, userState ->

        when (artworkState) {
            is State.Loading -> ArtworkUiState(state = State.Loading)
            is State.Error -> ArtworkUiState(state = State.Error())
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

    //region Init

    init {
        observeArtworkUseCase(artworkId = artworkId)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: ArtworkIntent) = viewModelScope.launch {
        when (intent) {
            //Navigation
            ArtworkIntent.OnBackTap -> _event.emit(ArtworkEvent.BackToPreviousScreen)
            is ArtworkIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            ArtworkIntent.OpenArtworkInfo -> openArtworkInfo()
            is ArtworkIntent.OpenEpisodeInfo -> _event.emit(OpenUrlInfo(url = intent.episode.infoUrl))

            // Dialogs
            ArtworkIntent.CloseDialog -> closeDialog()
            ArtworkIntent.ShowResetProgressDialog -> showResetProgressDialog()

            // Status
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            ArtworkIntent.ResetProgress -> resetProgress()

            // Other
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
            is ArtworkIntent.ExpandEpisodeDescription -> expandEpisodeDescription(episode = intent.episode)
            ArtworkIntent.CollapseEpisodeDescription -> collapseEpisodeDescription()
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
        ) ?: return State.Error()

        return State.Content(
            ArtworkContent(
                fullArtwork = dataState.fullArtwork,
                selectedMedia = selectedMedia,
                selectedSeason = season,
                expandedEpisodeId = userState.expandedEpisodeId,
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
                    ?: episodes.firstEpisodeToWatch()
            }
        }
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {

        if (!media.isAvailable) return

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

            _event.emit(OpenUrlInfo(url = url))
        }
    }

    private suspend fun changeWatchStatus(media: Media) {

        val status = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        changeMediaStatus(
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

            markPreviousAsWatched(episode = episode)

            state.copy(dialog = null)

        }

    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        artworkContent?.selectedMedia?.let { media ->
            saveProgress(media = media, progress = progress)
        }
    }

    private fun showResetProgressDialog() {
        _userState.update { it.copy(dialog = ArtworkDialog.ResetProgressConfirmation) }
    }

    private suspend fun resetProgress() {

        val fullArtwork = fullArtwork ?: return
        val selectedSeason = artworkContent?.selectedSeason

        resetProgress(artwork = fullArtwork.artwork, season = selectedSeason)

        _userState.update { state ->

            state.copy(
                selectedMedia = (fullArtwork as? FullArtwork.FullMovie)?.movie ?: episodes.filter { it.season == selectedSeason }.firstEpisode,
                dialog = null
            )

        }

    }

    private fun closeDialog() {
        _userState.update { it.copy(dialog = null) }
    }

    private fun expandEpisodeDescription(episode: Episode) {
        _userState.update { it.copy(expandedEpisodeId = episode.id) }
    }

    private fun collapseEpisodeDescription() {
        _userState.update { it.copy(expandedEpisodeId = null) }
    }

    //endregion

}