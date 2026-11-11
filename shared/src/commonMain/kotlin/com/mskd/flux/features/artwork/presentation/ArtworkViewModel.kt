package com.mskd.flux.features.artwork.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
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
    artworkId: Long,
    private val season: Int?,
    private val filesLauncher: FilesLauncher,
    private val urlLauncher: UrlLauncher,
    settingsDataStore: SettingsDataStore,
    tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    observeArtworkUseCase: ObserveArtworkUseCase,
    getPeopleUseCase: GetPeopleUseCase,
    private val changeMediaStatus: ChangeMediaStatusUseCase,
    private val markPreviousAsWatched: MarkPreviousAsWatchedUseCase,
    resetProgress: ResetProgressUseCase,
    private val resolvePlaybackAction: ResolvePlaybackActionUseCase,
    recordPlaybackResult: SaveProgressUseCase
) : ArtworkPageViewModel<ArtworkUiState, ArtworkIntent, ArtworkEvent>(
    artworkId = artworkId,
    observeArtworkUseCase = observeArtworkUseCase,
    tokenDataStore = tokenDataStore,
    catalogSyncCoordinator = catalogSyncCoordinator,
    resetProgressUseCase = resetProgress,
    recordPlaybackResult = recordPlaybackResult,
) {

    //region Computed properties

    private val artworkContent: ArtworkContent? get() = (uiState.value.state as? State.Content)?.content
    private val fullArtwork: FullArtwork? get() = artworkContent?.fullArtwork
    private val episodes: List<Episode> get() = (fullArtwork as? FullArtwork.FullShow)?.episodes.orEmpty()

    //endregion

    //region Flow

    private val _userState = MutableStateFlow(ArtworkUserState())

    private val settingsAndSync = combine(
        settingsDataStore.flow,
        tokenAndSync,
    ) { settings, (tokenAvailable, syncing) -> Triple(settings, tokenAvailable, syncing) }

    override val uiState: StateFlow<ArtworkUiState> = combine(
        observedArtwork,
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

    override suspend fun onIntent(intent: ArtworkIntent) {
        when (intent) {
            //Navigation
            ArtworkIntent.OnBackTap -> emit(ArtworkEvent.BackToPreviousScreen)
            is ArtworkIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            ArtworkIntent.OpenArtworkInfo -> openArtworkInfo()
            is ArtworkIntent.OpenEpisodeInfo -> urlLauncher.open(url = intent.episode.infoUrl)
            is ArtworkIntent.OpenFileExplorer -> filesLauncher.open(file = intent.media.file)
            is ArtworkIntent.OnPersonClick -> emit(ArtworkEvent.NavigateToPerson(person = intent.person))
            ArtworkIntent.OnArtworkImageClick -> onArtworkImageClick()

            // Dialogs
            ArtworkIntent.CloseDialog -> closeDialog()
            ArtworkIntent.ShowResetProgressDialog -> showResetProgressDialog()
            ArtworkIntent.ShowIdentificationDialog -> showIdentificationDialog()

            // Manual identification
            is ArtworkIntent.OnIdentified -> onIdentified(artworkId = intent.artworkId, type = intent.type)

            // Status
            is ArtworkIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            ArtworkIntent.MarkPreviousEpisodesAsWatched -> markPreviousEpisodesAsWatched()
            ArtworkIntent.ResetProgress -> resetProgress()

            // Other
            is ArtworkIntent.OnExternalPlayerResult -> onExternalPlayerResult(media = artworkContent?.selectedMedia, progress = intent.progress)
            is ArtworkIntent.ExpandEpisodeDescription -> expandEpisodeDescription(episode = intent.episode)
            ArtworkIntent.CollapseEpisodeDescription -> collapseEpisodeDescription()
            is ArtworkIntent.ToggleEpisodeSpoiler -> toggleEpisodeSpoiler(episode = intent.episode)
            ArtworkIntent.ToggleOverviewSpoiler -> toggleOverviewSpoiler()
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
        val spoilerIds = resolveSpoilerEpisodeIds(dataState)
        val readableIds = userState.revealedEpisodeIds + listOfNotNull(userState.expandedEpisodeId)
        val blurredIds = (spoilerIds - readableIds).toImmutableSet()
        // An Episode shown in ArtworkOverview shares its revealed state with the Episode itself.
        val overviewEpisodeId = (overview as? ArtworkOverviewContent.EpisodeOverview)?.episode?.id
        val isOverviewSpoiler = if (overviewEpisodeId != null) overviewEpisodeId in spoilerIds else resolveIsSeasonOverviewSpoiler(dataState)
        val isOverviewBlurred = if (overviewEpisodeId != null) overviewEpisodeId in blurredIds else isOverviewSpoiler && !userState.isOverviewRevealed

        return State.Content(
            ArtworkContent(
                fullArtwork = dataState.fullArtwork,
                selectedMedia = selectedMedia,
                selectedSeason = season,
                overview = overview,
                isOverviewSpoiler = isOverviewSpoiler,
                isOverviewBlurred = isOverviewBlurred,
                expandedEpisodeId = userState.expandedEpisodeId,
                spoilerEpisodeIds = spoilerIds,
                blurredEpisodeIds = blurredIds,
                isArtworkImageBlurred = (selectedMedia as? Episode)?.id in blurredIds,
                useExternalPlayer = dataState.useExternalPlayer,
                people = dataState.people,
                identification = dataState.identification,
                dialog = userState.dialog,
            )
        )
    }

    /** Movies only: the Manual identification of a Show is offered on its own page. A TMDB ID in a file name has priority. */
    private fun resolveIdentification(
        fullArtwork: FullArtwork,
        tokenAvailable: Boolean,
        syncing: Boolean,
    ): IdentificationAvailability {
        val movie = (fullArtwork as? FullArtwork.FullMovie)?.movie ?: return IdentificationAvailability.HIDDEN
        return IdentificationAvailability.of(artwork = fullArtwork.artwork, files = listOf(movie.file), tokenAvailable = tokenAvailable, syncing = syncing)
    }

    /** Only a Season description whose Episodes are all still to watch is a spoiler. */
    private fun resolveIsSeasonOverviewSpoiler(dataState: ArtworkDataState): Boolean {
        if (!dataState.blurSpoilers) return false
        val show = dataState.fullArtwork as? FullArtwork.FullShow ?: return false
        if (show.seasons.find { it.season == season }?.description.isNullOrEmpty()) return false
        val episodes = show.episodes.filter { it.season == season && !it.isUnknown }
        return episodes.isNotEmpty() && episodes.all { it.status == Status.TO_WATCH }
    }

    private fun resolveSpoilerEpisodeIds(dataState: ArtworkDataState): ImmutableSet<Long> {
        if (!dataState.blurSpoilers) return persistentSetOf()
        val episodes = (dataState.fullArtwork as? FullArtwork.FullShow)?.episodes ?: return persistentSetOf()
        return episodes
            .filter { it.status == Status.TO_WATCH && !it.isUnknown }
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

    /** While the selected Episode is a Spoiler, its Artwork image reveals / hides it instead of opening the Gallery. */
    private suspend fun onArtworkImageClick() {
        val content = artworkContent ?: return
        val episode = content.selectedMedia as? Episode
        if (episode != null && episode.id in content.spoilerEpisodeIds) toggleEpisodeSpoiler(episode = episode)
        else navigateToGallery()
    }

    private suspend fun navigateToGallery() {
        if (!tokenDataStore.tokenIsAvailable()) return
        val artwork = fullArtwork?.artwork ?: return
        emit(ArtworkEvent.NavigateToGallery(artworkId = artwork.id, contentType = artwork.type))
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {

        if (!media.isAvailable) return

        _userState.update { it.copy(selectedMedia = media) }

        when (val action = resolvePlaybackAction(media = media, forceInternal = forceInternal)) {
            is PlaybackAction.OpenPlayer -> emit(ArtworkEvent.PlayMedia(
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

    private fun showResetProgressDialog() {
        _userState.update { it.copy(dialog = ArtworkDialog.ResetProgressConfirmation) }
    }

    private fun showIdentificationDialog() {
        if (artworkContent?.identification != IdentificationAvailability.ENABLED) return
        _userState.update { it.copy(dialog = ArtworkDialog.Identification) }
    }

    private suspend fun onIdentified(artworkId: Long, type: ContentType) {
        _userState.update { it.copy(dialog = null) }
        emit(ArtworkEvent.Identified(artworkId = artworkId, type = type))
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

    /** An Episode shown in ArtworkOverview is revealed as the Episode itself, Artwork image included. */
    private fun toggleOverviewSpoiler() {
        when (val overview = artworkContent?.overview) {
            is ArtworkOverviewContent.EpisodeOverview -> toggleEpisodeSpoiler(episode = overview.episode)
            else -> _userState.update { it.copy(isOverviewRevealed = !it.isOverviewRevealed) }
        }
    }

    private fun collapseEpisodeDescription() {
        _userState.update { it.copy(expandedEpisodeId = null) }
    }

    //endregion

}