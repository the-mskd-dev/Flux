package com.mskd.flux.features.artwork.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.system.FilesLauncher
import com.mskd.flux.system.UrlLauncher
import com.mskd.flux.utils.extensions.firstEpisode
import com.mskd.flux.utils.extensions.firstEpisodeToWatch
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
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
    private val filesLauncher: FilesLauncher,
    private val urlLauncher: UrlLauncher,
    settingsDataStore: SettingsDataStore,
    private val tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    observeArtworkUseCase: ObserveArtworkUseCase,
    getPeopleUseCase: GetPeopleUseCase,
    private val changeMediaStatus: ChangeMediaStatusUseCase,
    private val markPreviousAsWatched: MarkPreviousAsWatchedUseCase,
    private val resetProgress: ResetProgressUseCase,
    private val resolvePlaybackAction: ResolvePlaybackActionUseCase,
    private val recordPlaybackResult: SaveProgressUseCase
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

    private val settingsAndSync = combine(
        settingsDataStore.flow,
        tokenDataStore.flow,
        catalogSyncCoordinator.state,
    ) { settings, token, sync -> Triple(settings, token.isNotBlank(), sync is SyncState.Syncing) }

    val uiState: StateFlow<ArtworkUiState> = combine(
        observeArtworkUseCase(artworkId = artworkId),
        settingsAndSync,
        getPeopleUseCase(artworkId = artworkId),
        _userState,
    ) { artworkState, (settings, tokenAvailable, syncing), people, userState ->

        when (artworkState) {
            is State.Loading -> ArtworkUiState(state = State.Loading)
            is State.Error -> ArtworkUiState(state = State.Error())
            is State.Content -> {

                val dataState = ArtworkDataState(
                    fullArtwork = artworkState.content,
                    useExternalPlayer = settings.externalPlayer,
                    blurSpoilers = settings.blurSpoilers,
                    people = people,
                    identification = resolveIdentification(
                        fullArtwork = artworkState.content,
                        tokenAvailable = tokenAvailable,
                        syncing = syncing,
                    ),
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

    //region Public Methods

    fun handleIntent(intent: ArtworkIntent) = viewModelScope.launch {
        when (intent) {
            //Navigation
            ArtworkIntent.OnBackTap -> _event.emit(ArtworkEvent.BackToPreviousScreen)
            is ArtworkIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            ArtworkIntent.OpenArtworkInfo -> openArtworkInfo()
            is ArtworkIntent.OpenEpisodeInfo -> urlLauncher.open(url = intent.episode.infoUrl)
            is ArtworkIntent.OpenFileExplorer -> filesLauncher.open(file = intent.media.file)
            is ArtworkIntent.OnPersonClick -> _event.emit(ArtworkEvent.NavigateToPerson(person = intent.person))
            ArtworkIntent.OnArtworkImageClick -> navigateToGallery()

            // Dialogs
            ArtworkIntent.CloseDialog -> closeDialog()
            ArtworkIntent.ShowResetProgressDialog -> showResetProgressDialog()
            ArtworkIntent.ShowIdentificationDialog -> showIdentificationDialog()

            // Identification manuelle
            is ArtworkIntent.OnIdentified -> onIdentified(artworkId = intent.artworkId)

            // Status
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            ArtworkIntent.ResetProgress -> resetProgress()

            // Other
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
            is ArtworkIntent.ExpandEpisodeDescription -> expandEpisodeDescription(episode = intent.episode)
            ArtworkIntent.CollapseEpisodeDescription -> collapseEpisodeDescription()
            is ArtworkIntent.ToggleEpisodeSpoiler -> toggleEpisodeSpoiler(episode = intent.episode)
            ArtworkIntent.ToggleOverviewSpoiler -> _userState.update { it.copy(isOverviewRevealed = !it.isOverviewRevealed) }
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

        val overview = resolveOverview(dataState.fullArtwork, selectedMedia, season)
        val spoilerIds = resolveSpoilerEpisodeIds(dataState, (overview as? ArtworkOverviewContent.EpisodeOverview)?.episode)
        val isOverviewSpoiler = resolveIsOverviewSpoiler(dataState)
        val readableIds = userState.revealedEpisodeIds + listOfNotNull(userState.expandedEpisodeId)

        return State.Content(
            ArtworkContent(
                fullArtwork = dataState.fullArtwork,
                selectedMedia = selectedMedia,
                selectedSeason = season,
                overview = overview,
                isOverviewSpoiler = isOverviewSpoiler,
                isOverviewBlurred = isOverviewSpoiler && !userState.isOverviewRevealed,
                expandedEpisodeId = userState.expandedEpisodeId,
                spoilerEpisodeIds = spoilerIds,
                blurredEpisodeIds = (spoilerIds - readableIds).toImmutableSet(),
                useExternalPlayer = dataState.useExternalPlayer,
                people = dataState.people,
                identification = dataState.identification,
                dialog = userState.dialog,
            )
        )
    }

    /** Movies only: the Identification manuelle of a Show is offered on its own page. A TMDB ID in a file name has priority. */
    private fun resolveIdentification(
        fullArtwork: FullArtwork,
        tokenAvailable: Boolean,
        syncing: Boolean,
    ): IdentificationAvailability {
        val movie = (fullArtwork as? FullArtwork.FullMovie)?.movie ?: return IdentificationAvailability.HIDDEN
        return when {
            fullArtwork.artwork.isUnknown || !tokenAvailable -> IdentificationAvailability.HIDDEN
            movie.file.nameProperties.tmdbId != null -> IdentificationAvailability.HIDDEN
            syncing -> IdentificationAvailability.DISABLED
            else -> IdentificationAvailability.ENABLED
        }
    }

    /** Only a Season description whose Episodes are all still to watch is a spoiler. */
    private fun resolveIsOverviewSpoiler(dataState: ArtworkDataState): Boolean {
        if (!dataState.blurSpoilers) return false
        val show = dataState.fullArtwork as? FullArtwork.FullShow ?: return false
        if (show.seasons.find { it.season == season }?.description.isNullOrEmpty()) return false
        val episodes = show.episodes.filter { it.season == season && !it.isUnknown }
        return episodes.isNotEmpty() && episodes.all { it.status == Status.TO_WATCH }
    }

    /** The Episode displayed in ArtworkOverview is excluded: its description is already visible there. */
    private fun resolveSpoilerEpisodeIds(dataState: ArtworkDataState, overviewEpisode: Episode?): ImmutableSet<Long> {
        if (!dataState.blurSpoilers) return persistentSetOf()
        val episodes = (dataState.fullArtwork as? FullArtwork.FullShow)?.episodes ?: return persistentSetOf()
        return episodes
            .filter { it.status == Status.TO_WATCH && !it.isUnknown && it.id != overviewEpisode?.id }
            .map { it.id }
            .toImmutableSet()
    }

    private fun resolveOverview(
        fullArtwork: FullArtwork,
        selectedMedia: Media,
        season: Int?,
    ): ArtworkOverviewContent {
        return when (fullArtwork) {
            is FullArtwork.FullMovie -> ArtworkOverviewContent.MovieOverview(
                movie = fullArtwork.movie,
                genres = fullArtwork.genres,
            )
            is FullArtwork.FullShow -> {
                val episode = selectedMedia as? Episode
                val seasonStarted = fullArtwork.episodes.filter { it.season == season }.any { it.status != Status.TO_WATCH }
                val seasonFinished = fullArtwork.episodes.filter { it.season == season }.all { it.status == Status.WATCHED }

                when {
                    episode != null && (episode.status == Status.IS_WATCHING || seasonStarted) && !seasonFinished -> {
                        ArtworkOverviewContent.EpisodeOverview(episode = episode)
                    }
                    else -> {
                        val description = fullArtwork.seasons.find { it.season == season }?.description
                            ?.takeIf { it.isNotEmpty() }
                            ?: fullArtwork.artwork.description.takeIf { it.isNotEmpty() }
                        ArtworkOverviewContent.Summary(description = description)
                    }
                }
            }
        }
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

    private suspend fun navigateToGallery() {
        if (!tokenDataStore.tokenIsAvailable()) return
        val artwork = fullArtwork?.artwork ?: return
        _event.emit(ArtworkEvent.NavigateToGallery(artworkId = artwork.id, contentType = artwork.type))
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {

        if (!media.isAvailable) return

        _userState.update { it.copy(selectedMedia = media) }

        when (val action = resolvePlaybackAction(media = media, forceInternal = forceInternal)) {
            is PlaybackAction.OpenPlayer -> _event.emit(ArtworkEvent.PlayMedia(
                media = action.media,
                externalPlayer = action.externalPlayer,
            ))
            PlaybackAction.Unavailable -> Unit
        }

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

            urlLauncher.open(url = url)
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
        artworkContent?.selectedMedia?.let {
            recordPlaybackResult(media = it, progress = progress)
        }
    }

    private fun showResetProgressDialog() {
        _userState.update { it.copy(dialog = ArtworkDialog.ResetProgressConfirmation) }
    }

    private fun showIdentificationDialog() {
        if (artworkContent?.identification != IdentificationAvailability.ENABLED) return
        _userState.update { it.copy(dialog = ArtworkDialog.Identification) }
    }

    private suspend fun onIdentified(artworkId: Long) {
        _userState.update { it.copy(dialog = null) }
        _event.emit(ArtworkEvent.ReplaceArtwork(artworkId = artworkId))
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

    private fun toggleEpisodeSpoiler(episode: Episode) {
        _userState.update {
            val revealed = it.revealedEpisodeIds
            it.copy(
                revealedEpisodeIds = (if (episode.id in revealed) revealed - episode.id else revealed + episode.id)
                    .toImmutableSet()
            )
        }
    }

    private fun collapseEpisodeDescription() {
        _userState.update { it.copy(expandedEpisodeId = null) }
    }

    //endregion

}