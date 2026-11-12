package com.mskd.flux.features.movie.presentation

import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.presentation.ArtworkViewModel
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.system.FilesLauncher
import com.mskd.flux.system.UrlLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Movie page: a film, its people, and the actions on its file. */
class MovieViewModel(
    artworkId: Long,
    private val filesLauncher: FilesLauncher,
    private val urlLauncher: UrlLauncher,
    tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    observeArtworkUseCase: ObserveArtworkUseCase,
    getPeopleUseCase: GetPeopleUseCase,
    private val changeMediaStatus: ChangeMediaStatusUseCase,
    resetProgress: ResetProgressUseCase,
    private val resolvePlaybackAction: ResolvePlaybackActionUseCase,
    recordPlaybackResult: SaveProgressUseCase,
) : ArtworkViewModel<MovieUiState, MovieIntent, MovieEvent>(
    artworkId = artworkId,
    observeArtworkUseCase = observeArtworkUseCase,
    tokenDataStore = tokenDataStore,
    catalogSyncCoordinator = catalogSyncCoordinator,
    resetProgressUseCase = resetProgress,
    recordPlaybackResult = recordPlaybackResult,
) {

    //region Flow

    private val dialog = MutableStateFlow<MoviePageDialog?>(null)

    override val uiState: StateFlow<MovieUiState> = combine(
        observedArtwork,
        tokenAndSync,
        getPeopleUseCase(artworkId = artworkId),
        dialog,
    ) { artworkState, (tokenAvailable, syncing), people, dialog ->

        when (artworkState) {
            is State.Loading -> MovieUiState(state = State.Loading)
            is State.Error -> MovieUiState(state = State.Error())
            is State.Content -> {
                // A series has its own page
                val fullMovie = artworkState.content as? FullArtwork.FullMovie
                    ?: return@combine MovieUiState(state = State.Error())
                MovieUiState(
                    state = State.Content(
                        MovieContent(
                            fullMovie = fullMovie,
                            overview = ArtworkOverviewContent.MovieOverview(movie = fullMovie.movie, genres = fullMovie.genres),
                            people = people,
                            // A TMDB ID in a file name has priority over the Manual identification
                            identification = IdentificationAvailability.of(
                                artwork = fullMovie.artwork,
                                files = listOf(fullMovie.movie.file),
                                tokenAvailable = tokenAvailable,
                                syncing = syncing,
                            ),
                            dialog = dialog,
                        )
                    )
                )
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovieUiState()
    )

    //endregion

    //region Computed properties

    private val movieContent: MovieContent? get() = (uiState.value.state as? State.Content)?.content

    //endregion

    //region Public Methods

    override suspend fun onIntent(intent: MovieIntent) {
        when (intent) {
            // Navigation
            MovieIntent.OnBackTap -> emit(MovieEvent.BackToPreviousScreen)
            is MovieIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            MovieIntent.OpenMovieInfo -> movieContent?.let { urlLauncher.open(url = it.fullMovie.artwork.infoUrl) }
            is MovieIntent.OpenFileExplorer -> filesLauncher.open(file = intent.media.file)
            is MovieIntent.OnPersonClick -> emit(MovieEvent.NavigateToPerson(person = intent.person))
            MovieIntent.OnArtworkImageClick -> navigateToGallery()

            // Dialogs
            MovieIntent.CloseDialog -> dialog.value = null
            MovieIntent.ShowResetProgressDialog -> dialog.value = MoviePageDialog.ResetProgressConfirmation
            MovieIntent.ShowIdentificationDialog -> showIdentificationDialog()

            // Manual identification
            is MovieIntent.OnIdentified -> onIdentified(artworkId = intent.artworkId, type = intent.type)

            // Status
            is MovieIntent.ChangeWatchStatus -> changeWatchStatus(media = intent.media)
            MovieIntent.ResetProgress -> resetProgress()

            // Other
            is MovieIntent.OnExternalPlayerResult -> onExternalPlayerResult(media = movieContent?.fullMovie?.movie, progress = intent.progress)
        }
    }

    //endregion

    //region Private Methods

    private suspend fun navigateToGallery() {
        if (!tokenDataStore.tokenIsAvailable()) return
        val artwork = movieContent?.fullMovie?.artwork ?: return
        emit(MovieEvent.NavigateToGallery(artworkId = artwork.id, contentType = artwork.type))
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        if (!media.isAvailable) return
        when (val action = resolvePlaybackAction(media = media, forceInternal = forceInternal)) {
            is PlaybackAction.OpenPlayer -> emit(MovieEvent.PlayMedia(media = action.media, externalPlayer = action.externalPlayer))
            PlaybackAction.Unavailable -> Unit
        }
    }

    private suspend fun changeWatchStatus(media: Media) {
        val status = if (media.status != Status.WATCHED) Status.WATCHED else Status.TO_WATCH
        changeMediaStatus(media = media, status = status)
    }

    private fun showIdentificationDialog() {
        if (movieContent?.identification != IdentificationAvailability.ENABLED) return
        dialog.value = MoviePageDialog.Identification
    }

    private suspend fun onIdentified(artworkId: Long, type: ContentType) {
        dialog.value = null
        emit(MovieEvent.Identified(artworkId = artworkId, type = type))
    }

    private suspend fun resetProgress() {
        val artwork = movieContent?.fullMovie?.artwork ?: return
        resetProgress(artwork = artwork, season = null)
        dialog.value = null
    }

    //endregion

}
