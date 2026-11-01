package com.mskd.flux.features.show.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.system.UrlLauncher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowViewModel(
    private val artworkId: Long,
    private val urlLauncher: UrlLauncher,
    observeArtworkUseCase: ObserveArtworkUseCase,
    getPeopleUseCase: GetPeopleUseCase,
    private val tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    private val resetProgress: ResetProgressUseCase,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<ShowEvent>()
    val event = _event.asSharedFlow()

    private val _userState = MutableStateFlow<ShowDialog?>(null)

    private val tokenAndSync = combine(
        tokenDataStore.flow,
        catalogSyncCoordinator.state,
    ) { token, sync -> token.isNotBlank() to (sync is SyncState.Syncing) }

    val uiState: StateFlow<ShowUiState> = combine(
        observeArtworkUseCase(artworkId = artworkId),
        getPeopleUseCase(artworkId = artworkId),
        tokenAndSync,
        _userState,
    ) { artworkState, people, (tokenAvailable, syncing), dialog ->
        when (artworkState) {
            is State.Loading -> ShowUiState(state = State.Loading)
            is State.Error -> ShowUiState(state = State.Error())
            is State.Content -> {
                val fullShow = artworkState.content as? FullArtwork.FullShow
                    ?: return@combine ShowUiState(state = State.Error())
                ShowUiState(
                    state = State.Content(
                        ShowContent(
                            fullShow = fullShow,
                            people = people,
                            identification = IdentificationAvailability.of(
                                artwork = fullShow.artwork,
                                files = fullShow.episodes.map { it.file },
                                tokenAvailable = tokenAvailable,
                                syncing = syncing,
                            ),
                            dialog = dialog
                        )
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShowUiState()
    )

    //endregion

    //region Computed properties

    private val showContent get() = (uiState.value.state as? State.Content)?.content

    //endregion

    //region Public Methods

    fun handleIntent(intent: ShowIntent) = viewModelScope.launch {
        when (intent) {
            // Navigation
            ShowIntent.OnBackTap -> _event.emit(ShowEvent.BackToPreviousScreen)
            is ShowIntent.OnSeasonClick -> navigateToSeason(season = intent.season, rgb = intent.rgb)
            is ShowIntent.OnPersonClick -> _event.emit(ShowEvent.NavigateToPerson(person = intent.person))
            ShowIntent.OnArtworkImageClick -> navigateToGallery()

            // Dialogs
            ShowIntent.CloseDialog -> closeDialog()
            is ShowIntent.ShowSeasonPreview -> showSeasonPreview(season = intent.season)
            is ShowIntent.ShowResetProgressDialog -> showResetDialog()
            ShowIntent.ShowIdentificationDialog -> showIdentificationDialog()

            // Identification manuelle
            is ShowIntent.OnIdentified -> onIdentified(artworkId = intent.artworkId)

            // Other
            ShowIntent.OpenShowInfo -> openShowInfo()
            ShowIntent.ResetProgress -> resetProgress()
        }
    }

    //endregion

    //region Private Methods

    private suspend fun navigateToGallery() {
        if (!tokenDataStore.tokenIsAvailable()) return
        _event.emit(ShowEvent.NavigateToGallery(artworkId = artworkId, contentType = ContentType.SHOW))
    }

    private suspend fun navigateToSeason(season: Int, rgb: Int?) {
        _event.emit(ShowEvent.NavigateToSeason(artworkId = artworkId, season = season, rgb = rgb))
    }

    private fun closeDialog() {
        _userState.update { null }
    }

    private fun showSeasonPreview(season: Season) {
        _userState.update { ShowDialog.SeasonPreview(season = season) }
    }

    private fun showResetDialog() {
        _userState.update { ShowDialog.ResetProgress }
    }

    private fun showIdentificationDialog() {
        if (showContent?.identification != IdentificationAvailability.ENABLED) return
        _userState.update { ShowDialog.Identification }
    }

    private suspend fun onIdentified(artworkId: Long) {
        _userState.update { null }
        _event.emit(ShowEvent.ReplaceShow(artworkId = artworkId))
    }

    private suspend fun openShowInfo() {
        val fullShow = showContent?.fullShow ?: return

        urlLauncher.open(url = fullShow.artwork.infoUrl)
    }

    private suspend fun resetProgress() {
        val fullShow = showContent?.fullShow ?: return

        resetProgress(artwork = fullShow.artwork, season = null)
        _userState.update { null }
    }

    //endregion
}