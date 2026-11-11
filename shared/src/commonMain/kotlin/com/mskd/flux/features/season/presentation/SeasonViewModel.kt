package com.mskd.flux.features.season.presentation

import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.presentation.ArtworkPageViewModel
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
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
import com.mskd.flux.utils.extensions.firstEpisodeToWatch
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Season page: the Episode of one [season] of a Show, with their Spoiler masking and their actions. */
class SeasonViewModel(
    artworkId: Long,
    private val season: Int,
    private val filesLauncher: FilesLauncher,
    private val urlLauncher: UrlLauncher,
    settingsDataStore: SettingsDataStore,
    tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    observeArtworkUseCase: ObserveArtworkUseCase,
    private val changeMediaStatus: ChangeMediaStatusUseCase,
    private val markPreviousAsWatched: MarkPreviousAsWatchedUseCase,
    resetProgress: ResetProgressUseCase,
    private val resolvePlaybackAction: ResolvePlaybackActionUseCase,
    recordPlaybackResult: SaveProgressUseCase,
) : ArtworkPageViewModel<SeasonUiState, SeasonIntent, SeasonEvent>(
    artworkId = artworkId,
    observeArtworkUseCase = observeArtworkUseCase,
    tokenDataStore = tokenDataStore,
    catalogSyncCoordinator = catalogSyncCoordinator,
    resetProgressUseCase = resetProgress,
    recordPlaybackResult = recordPlaybackResult,
) {

    private data class UserState(
        val selectedEpisodeId: Long? = null,
        val expandedEpisodeId: Long? = null,
        val revealedEpisodeIds: Set<Long> = emptySet(),
        val isOverviewRevealed: Boolean = false,
        val dialog: SeasonPageDialog? = null,
    )

    //region Flow

    private val _userState = MutableStateFlow(UserState())

    override val uiState: StateFlow<SeasonUiState> = combine(
        observedArtwork,
        settingsDataStore.flow,
        tokenAndSync,
        _userState,
    ) { artworkState, settings, (tokenAvailable, syncing), userState ->

        when (artworkState) {
            is State.Loading -> SeasonUiState(state = State.Loading)
            is State.Error -> SeasonUiState(state = State.Error())
            is State.Content -> {
                val fullShow = artworkState.content as? FullArtwork.FullShow
                    ?: return@combine SeasonUiState(state = State.Error())
                val identification = IdentificationAvailability.of(
                    artwork = fullShow.artwork,
                    files = fullShow.episodes.map { it.file },
                    tokenAvailable = tokenAvailable,
                    syncing = syncing,
                )
                SeasonUiState(state = buildContent(fullShow, settings.blurSpoilers, identification, userState))
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeasonUiState()
    )

    //endregion

    //region Computed properties

    private val seasonContent: SeasonContent? get() = (uiState.value.state as? State.Content)?.content

    //endregion

    //region Public Methods

    override suspend fun onIntent(intent: SeasonIntent) {
        when (intent) {
            // Navigation
            SeasonIntent.OnBackTap -> emit(SeasonEvent.BackToPreviousScreen)
            is SeasonIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            SeasonIntent.OpenSeasonInfo -> openSeasonInfo()
            is SeasonIntent.OpenEpisodeInfo -> urlLauncher.open(url = intent.episode.infoUrl)
            is SeasonIntent.OpenFileExplorer -> filesLauncher.open(file = intent.media.file)
            SeasonIntent.OnArtworkImageClick -> onArtworkImageClick()

            // Dialogs
            SeasonIntent.CloseDialog -> setDialog(null)
            SeasonIntent.ShowResetProgressDialog -> setDialog(SeasonPageDialog.ResetProgressConfirmation)
            SeasonIntent.ShowIdentificationDialog -> showIdentificationDialog()

            // Manual identification
            is SeasonIntent.OnIdentified -> onIdentified(artworkId = intent.artworkId, type = intent.type)

            // Status
            is SeasonIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            SeasonIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            SeasonIntent.ResetProgress -> resetProgress()

            // Other
            is SeasonIntent.OnExternalPlayerResult -> onExternalPlayerResult(media = seasonContent?.selectedEpisode, progress = intent.progress)
            is SeasonIntent.ExpandEpisodeDescription -> _userState.update { it.copy(expandedEpisodeId = intent.episode.id) }
            SeasonIntent.CollapseEpisodeDescription -> _userState.update { it.copy(expandedEpisodeId = null) }
            is SeasonIntent.ToggleEpisodeSpoiler -> toggleEpisodeSpoiler(episode = intent.episode)
            SeasonIntent.ToggleOverviewSpoiler -> toggleOverviewSpoiler()
        }
    }

    //endregion

    //region Private Methods

    private fun buildContent(
        fullShow: FullArtwork.FullShow,
        blurSpoilers: Boolean,
        identification: IdentificationAvailability,
        userState: UserState,
    ): State<SeasonContent> {

        val episodes = fullShow.episodes.filter { it.season == season }.sortedBy { it.number }
        val selectedEpisode = episodes.firstOrNull { it.id == userState.selectedEpisodeId }
            ?: episodes.firstEpisodeToWatch()
            ?: return State.Error()

        val overview = resolveOverview(fullShow, episodes, selectedEpisode)
        val spoilerIds = resolveSpoilerEpisodeIds(episodes, blurSpoilers)
        val readableIds = userState.revealedEpisodeIds + listOfNotNull(userState.expandedEpisodeId)
        val blurredIds = (spoilerIds - readableIds).toImmutableSet()
        // An Episode shown in ArtworkOverview shares its revealed state with the Episode itself.
        val overviewEpisodeId = (overview as? ArtworkOverviewContent.EpisodeOverview)?.episode?.id
        val isOverviewSpoiler = if (overviewEpisodeId != null) overviewEpisodeId in spoilerIds else isSeasonOverviewSpoiler(fullShow, episodes, blurSpoilers)
        val isOverviewBlurred = if (overviewEpisodeId != null) overviewEpisodeId in blurredIds else isOverviewSpoiler && !userState.isOverviewRevealed

        return State.Content(
            SeasonContent(
                fullShow = fullShow,
                season = season,
                episodes = episodes.toImmutableList(),
                selectedEpisode = selectedEpisode,
                overview = overview,
                isOverviewSpoiler = isOverviewSpoiler,
                isOverviewBlurred = isOverviewBlurred,
                expandedEpisodeId = userState.expandedEpisodeId,
                spoilerEpisodeIds = spoilerIds,
                blurredEpisodeIds = blurredIds,
                isArtworkImageBlurred = selectedEpisode.id in blurredIds,
                identification = identification,
                dialog = userState.dialog,
            )
        )
    }

    /** Only a Season description whose Episode are all still to watch is a spoiler. */
    private fun isSeasonOverviewSpoiler(fullShow: FullArtwork.FullShow, episodes: List<Episode>, blurSpoilers: Boolean): Boolean {
        if (!blurSpoilers) return false
        if (fullShow.season(season)?.description.isNullOrEmpty()) return false
        val known = episodes.filter { !it.isUnknown }
        return known.isNotEmpty() && known.all { it.status == Status.TO_WATCH }
    }

    private fun resolveSpoilerEpisodeIds(episodes: List<Episode>, blurSpoilers: Boolean): ImmutableSet<Long> {
        if (!blurSpoilers) return persistentSetOf()
        return episodes
            .filter { it.status == Status.TO_WATCH && !it.isUnknown }
            .map { it.id }
            .toImmutableSet()
    }

    private fun resolveOverview(
        fullShow: FullArtwork.FullShow,
        episodes: List<Episode>,
        selectedEpisode: Episode,
    ): ArtworkOverviewContent {
        val seasonStarted = episodes.any { it.status != Status.TO_WATCH }
        val seasonFinished = episodes.all { it.status == Status.WATCHED }
        return if ((selectedEpisode.status == Status.IS_WATCHING || seasonStarted) && !seasonFinished) {
            ArtworkOverviewContent.EpisodeOverview(episode = selectedEpisode)
        } else {
            val description = fullShow.season(season)?.description?.takeIf { it.isNotEmpty() }
                ?: fullShow.artwork.description.takeIf { it.isNotEmpty() }
            ArtworkOverviewContent.Summary(description = description)
        }
    }

    /** While the selected Episode is a Spoiler, its Artwork image reveals / hides it instead of opening the Gallery. */
    private suspend fun onArtworkImageClick() {
        val content = seasonContent ?: return
        val episode = content.selectedEpisode
        if (episode.id in content.spoilerEpisodeIds) toggleEpisodeSpoiler(episode = episode)
        else navigateToGallery()
    }

    /** The Gallery shows the images of the Show itself, not those of the Season. */
    private suspend fun navigateToGallery() {
        if (!tokenDataStore.tokenIsAvailable()) return
        emit(SeasonEvent.NavigateToGallery(artworkId = artworkId, contentType = ContentType.SHOW))
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {

        if (!media.isAvailable) return

        _userState.update { it.copy(selectedEpisodeId = media.mediaId) }

        when (val action = resolvePlaybackAction(media = media, forceInternal = forceInternal)) {
            is PlaybackAction.OpenPlayer -> emit(SeasonEvent.PlayMedia(media = action.media, externalPlayer = action.externalPlayer))
            PlaybackAction.Unavailable -> Unit
        }

    }

    private suspend fun openSeasonInfo() {
        val url = seasonContent?.fullShow?.season(season)?.infoUrl ?: return
        urlLauncher.open(url = url)
    }

    private suspend fun changeWatchStatus(media: Media) {

        val status = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH

        changeMediaStatus(media = media, status = status)

        val allEpisodes = seasonContent?.fullShow?.episodes.orEmpty()
        if (
            status == Status.WATCHED
            && media is Episode
            && allEpisodes.getPreviousEpisodesFor(media).any { it.status != Status.WATCHED }
        ) {
            setDialog(SeasonPageDialog.EpisodeStatusConfirmation(episode = media))
        }

    }

    private suspend fun markPreviousEpisodesAsWatched() {
        val episode = (_userState.value.dialog as? SeasonPageDialog.EpisodeStatusConfirmation)?.episode ?: return
        markPreviousAsWatched(episode = episode)
        setDialog(null)
    }

    private fun showIdentificationDialog() {
        if (seasonContent?.identification != IdentificationAvailability.ENABLED) return
        setDialog(SeasonPageDialog.Identification)
    }

    private suspend fun onIdentified(artworkId: Long, type: ContentType) {
        setDialog(null)
        emit(SeasonEvent.Identified(artworkId = artworkId, type = type))
    }

    private suspend fun resetProgress() {
        val content = seasonContent ?: return
        resetProgress(artwork = content.fullShow.artwork, season = season)
        _userState.update { it.copy(selectedEpisodeId = content.episodes.first().id, dialog = null) }
    }

    private fun setDialog(dialog: SeasonPageDialog?) {
        _userState.update { it.copy(dialog = dialog) }
    }

    private fun toggleEpisodeSpoiler(episode: Episode) {
        _userState.update {
            val revealed = it.revealedEpisodeIds
            it.copy(revealedEpisodeIds = if (episode.id in revealed) revealed - episode.id else revealed + episode.id)
        }
    }

    /** An Episode shown in ArtworkOverview is revealed as the Episode itself, Artwork image included. */
    private fun toggleOverviewSpoiler() {
        when (val overview = seasonContent?.overview) {
            is ArtworkOverviewContent.EpisodeOverview -> toggleEpisodeSpoiler(episode = overview.episode)
            else -> _userState.update { it.copy(isOverviewRevealed = !it.isOverviewRevealed) }
        }
    }

    //endregion

}
