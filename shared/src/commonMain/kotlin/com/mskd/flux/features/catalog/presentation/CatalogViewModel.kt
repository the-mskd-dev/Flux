package com.mskd.flux.features.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.core.Flavor
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.model.CatalogPreferences
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToHowTo
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToMovie
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToPrivateFolder
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSearch
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSettings
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToShow
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSources
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToToken
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToUnknown
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.domain.usecase.GetHistoryUseCase
import com.mskd.flux.features.player.domain.playback.PlaybackLaunch
import com.mskd.flux.features.player.domain.playback.PlaybackOutcome
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.report.Trace
import com.mskd.flux.system.UpdateManager
import com.mskd.flux.system.UrlLauncher
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.filterFor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val artworkDb: DatabaseRepository,
    private val detailsDb: DetailsRepository,
    private val historyDb: HistoryRepository,
    private val userDataStore: UserDataStore,
    private val tokenDataStore: TokenDataStore,
    private val catalogDataStore: CatalogDataStore,
    private val privateFolderDataStore: PrivateFolderDataStore,
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val appInfo: AppInfo,
    private val urlLauncher: UrlLauncher,
    private val playbackLaunch: PlaybackLaunch,
    private val setArtworkPrivacy: SetArtworkPrivacyUseCase
): ViewModel() {

    private val _event = MutableSharedFlow<CatalogEvent>()
    val event = _event.asSharedFlow()

    private val _showSortingSheet = MutableStateFlow(false)
    private val _showViewModeSheet = MutableStateFlow(false)
    private val _showMessageDialog = MutableStateFlow(false)

    // TODO: Remove the Play Store announcement (dialog, flag, view model logic, strings) by March 2027
    private val _showPlayStorePromo = MutableStateFlow(false)

    private val preferencesFlow = combine(
        getHistoryUseCase(),
        catalogDataStore.flow,
        tokenDataStore.flow,
        privateFolderDataStore.flow
    ) { history, catalog, token, privateFolder  ->
        CatalogPreferences(
            history = history.toImmutableList(),
            sortingMode = catalog.sortingMode,
            viewMode = catalog.viewMode,
            token = token,
            privateFolderEnabled = privateFolder.enabled,
        )
    }

    private val artworkFlow = combine(
        artworkDb.flowArtworks(),
        detailsDb.flowGenres()
    ) { artworks, genres ->
        artworks to genres.filterFor(artworks = artworks)
    }

    private val viewFlow = combine(
        _showSortingSheet,
        _showViewModeSheet,
        _showPlayStorePromo
    ) { sort, view, promo ->
        Triple(sort, view, promo)
    }

    val uiState: StateFlow<CatalogUiState> = combine(
        artworkFlow,
        syncCatalogUseCase.state,
        preferencesFlow,
        viewFlow,
        _showMessageDialog,
    ) { (artworks, genres), syncState, preferences, (showSortingSheet, showViewModeSheet, showPlayStorePromo), showMessageDialog  ->

        if (syncState is SyncState.Syncing && syncState.type == SyncType.FULL) {

            CatalogUiState(
                state = CatalogState.Loading(syncState = syncState),
            )

        } else {

            val sortedArtworks = when (preferences.sortingMode) {
                CatalogSortingMode.LAST_MODIFICATION -> artworks.sortedByDescending { it.lastModification }
                CatalogSortingMode.A_TO_Z -> artworks.sortedBy { it.title }
                CatalogSortingMode.Z_TO_A -> artworks.sortedByDescending { it.title }
            }

            CatalogUiState(
                state = CatalogState.Content(
                    artworks = sortedArtworks.toImmutableList(),
                    genres = genres.toImmutableList(),
                    history = preferences.history,
                    isRefreshing = syncState is SyncState.Syncing,
                    tokenIsMissing = preferences.token.isBlank(),
                    privateFolderEnabled = preferences.privateFolderEnabled,
                    sortingMode = preferences.sortingMode,
                    viewMode = preferences.viewMode,
                    showSortingSheet = showSortingSheet,
                    showViewSheet = showViewModeSheet,
                    showPlayStorePromo = showPlayStorePromo,
                    message = CatalogMessageState(),
                ),
            )

        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState()
    )


    init {
        viewModelScope.launch {
            syncCatalog()
        }
        viewModelScope.launch { setUpPlayStorePromo() }
    }

    fun handleIntent(intent: CatalogIntent) = viewModelScope.launch {
        when (intent) {

            // Navigation
            is CatalogIntent.SyncCatalog -> syncCatalog()
            is CatalogIntent.OnArtworkClick -> onArtworkClick(artwork = intent.artwork, rgb = intent.rgb)
            is CatalogIntent.OnCategoryClick -> _event.emit(NavigateToSearch(category = intent.category))
            is CatalogIntent.OnGenreClick -> _event.emit(NavigateToSearch(genre = intent.genre))
            CatalogIntent.OnSearchClick -> _event.emit(NavigateToSearch())
            CatalogIntent.OnSettingsClick -> _event.emit(NavigateToSettings)
            CatalogIntent.OnHowToClick -> _event.emit(NavigateToHowTo)
            CatalogIntent.OnSourcesClick -> _event.emit(NavigateToSources)
            CatalogIntent.OnTokenClick -> _event.emit(NavigateToToken)
            CatalogIntent.OnPrivateFolderClick -> _event.emit(NavigateToPrivateFolder)

            // Private folder
            is CatalogIntent.AddArtworkToPrivateFolder -> addArtworkToPrivateFolder(artwork = intent.artwork)

            // Sort
            is CatalogIntent.SelectSortingMode -> selectSortingOption(mode = intent.mode)
            is CatalogIntent.ShowSortingModes -> showSortingOptions(show = intent.show)

            // View mode
            is CatalogIntent.SelectViewMode -> selectViewMode(mode = intent.mode)
            is CatalogIntent.ShowViewModes -> showViewModes(show = intent.show)

            // History
            is CatalogIntent.DeleteHistoryEntry -> deleteHistoryEntry(entry = intent.entry)
            is CatalogIntent.ShowDetails -> showDetails(media = intent.media)

            // Play Store announcement
            CatalogIntent.OnPlayStorePromoDismiss -> _showPlayStorePromo.update { false }
            CatalogIntent.OnPlayStorePromoClick -> {
                urlLauncher.open(url = Constants.CONTACT.PLAY_STORE)
                _showPlayStorePromo.update { false }
            }

            // Player
            is CatalogIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
        }
    }

    // TODO: Remove by March 2027 (announcement)
    private suspend fun setUpPlayStorePromo() {

        if (appInfo.flavor != Flavor.FOSS) return

        // TODO: Remove this debug override when going to production, so the flag is honored in debug builds too
        val alreadySeen = !appInfo.isDebug && catalogDataStore.flow.first().playStorePromoSeen
        if (alreadySeen) return

        _showPlayStorePromo.value = true

        // Mark it as seen as soon as it is really displayed (Content state), not on dismissal
        uiState.first { (it.state as? CatalogState.Content)?.showPlayStorePromo == true }
        catalogDataStore.setPlayStorePromoSeen()

    }

    private suspend fun syncCatalog() {

        val fullSyncNeeded = UpdateManager.fullSyncIsNeeded(
            lastSyncVersionCode = userDataStore.getVersionCode(),
            currentVersionCode = appInfo.versionCode
        )

        if (fullSyncNeeded) {
            Trace.info("CatalogViewModel", "Full sync requested")
        }

        syncCatalogUseCase(type = if (fullSyncNeeded) SyncType.FULL else SyncType.LIGHT)

    }

    private suspend fun onArtworkClick(artwork: Artwork, rgb: Int?) {

        val event = when {
            artwork.id == Artwork.UNKNOWN_ID -> NavigateToUnknown
            artwork.type == ContentType.SHOW -> NavigateToShow(artworkId = artwork.id, rgb = rgb)
            else -> NavigateToMovie(artworkId = artwork.id, rgb = rgb)
        }

        _event.emit(event)

    }

    private suspend fun selectSortingOption(mode: CatalogSortingMode) {
        catalogDataStore.setSortingMode(mode = mode)
        _showSortingSheet.update { false }
    }

    private fun showSortingOptions(show: Boolean) {
        _showSortingSheet.update { show }
    }

    private suspend fun selectViewMode(mode: CatalogViewMode) {
        catalogDataStore.setViewMode(mode = mode)
        _showViewModeSheet.update { false }
    }

    private fun showViewModes(show: Boolean) {
        _showViewModeSheet.update { show }
    }

    private suspend fun showDetails(media: Media) {

        val artwork = artworkFlow.firstOrNull()?.first?.find {
            it.id == media.artworkId
        } ?: return

        onArtworkClick(artwork = artwork, rgb = null)

    }

    private suspend fun addArtworkToPrivateFolder(artwork: Artwork) {
        setArtworkPrivacy(artworkId = artwork.id, isPrivate = true)
    }

    private suspend fun deleteHistoryEntry(entry: HistoryEntry) {
        historyDb.delete(artworkId = entry.media.artworkId)
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        when (val outcome = playbackLaunch.play(media = media, forceInternal = forceInternal)) {
            is PlaybackOutcome.OpenInternalPlayer -> _event.emit(CatalogEvent.OpenInternalPlayer(media = outcome.media))
            PlaybackOutcome.HandedToExternalPlayer, PlaybackOutcome.Unavailable -> Unit
        }
    }

}
