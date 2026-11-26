package com.mskd.flux.features.unknown.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.player.domain.playback.PlaybackLaunch
import com.mskd.flux.features.player.domain.playback.PlaybackOutcome
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.system.FilesLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnknownViewModel(
    private val filesLauncher: FilesLauncher,
    observeArtworkUseCase: ObserveArtworkUseCase,
    settingsDataStore: SettingsDataStore,
    tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    private val playbackLaunch: PlaybackLaunch,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<UnknownEvent>()
    val event = _event.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dialog = MutableStateFlow<UnknownDialog?>(null)

    private val identificationAvailability: Flow<IdentificationAvailability> = combine(
        tokenDataStore.flow,
        catalogSyncCoordinator.state,
    ) { token, sync ->
        IdentificationAvailability.of(
            tokenAvailable = token.isNotBlank(),
            syncing = sync is SyncState.Syncing,
        )
    }

    val uiState: StateFlow<UnknownUiState> = combine(
        observeArtworkUseCase(artworkId = Artwork.UNKNOWN_ID),
        settingsDataStore.flow,
        _searchQuery,
        identificationAvailability,
        _dialog,
    ) { artworkContent, settings, searchQuery, identification, dialog ->

        val fullShow = (artworkContent as? State.Content)?.content as? FullArtwork.FullShow
        UnknownUiState(
            screen = State.Content(Unit),
            searchQuery = searchQuery,
            medias = fullShow?.episodes?.sortedWith(
                compareBy<Episode> { it.title }.thenBy { it.season }.thenBy { it.number }
            ) ?: emptyList(),
            useExternalPlayer = settings.externalPlayer,
            identification = identification,
            dialog = dialog,
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UnknownUiState()
    )

    //endregion

    //region Public Methods

    fun handleIntent(intent: UnknownIntent) = viewModelScope.launch {
        when (intent) {
            is UnknownIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            is UnknownIntent.OpenFileExplorer -> filesLauncher.open(file = intent.media.file)
            UnknownIntent.OnBackTap -> _event.emit(UnknownEvent.BackToPreviousScreen)
            UnknownIntent.OnInfoTap -> _event.emit(UnknownEvent.NavigateToHowToScreen)
            is UnknownIntent.DoSearch -> doSearch(query = intent.query)
            is UnknownIntent.ShowIdentificationDialog -> showIdentificationDialog(media = intent.media)
            UnknownIntent.CloseDialog, UnknownIntent.OnIdentified -> closeDialog()
        }
    }

    //endregion

    //region Private Methods

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        when (val outcome = playbackLaunch.play(media = media, forceInternal = forceInternal)) {
            is PlaybackOutcome.OpenInternalPlayer -> _event.emit(UnknownEvent.OpenInternalPlayer(media = outcome.media))
            PlaybackOutcome.HandedToExternalPlayer, PlaybackOutcome.Unavailable -> Unit
        }
    }

    private fun doSearch(query: String) {
        _searchQuery.update { query }
    }

    private fun showIdentificationDialog(media: Episode) {
        if (uiState.value.identification != IdentificationAvailability.ENABLED) return
        _dialog.update { UnknownDialog.Identification(media = media) }
    }

    private fun closeDialog() {
        _dialog.update { null }
    }

    //endregion

}