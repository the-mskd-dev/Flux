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
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.model.CatalogPreferences
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToHowTo
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToMovie
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSearch
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSettings
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToShow
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToSources
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToToken
import com.mskd.flux.features.catalog.presentation.CatalogEvent.NavigateToUnknown
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.RecordPlaybackResultUseCase
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.UpdateManager
import com.mskd.flux.utils.extensions.filterFor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val artworkDb: DatabaseRepository,
    private val detailsDb: DetailsRepository,
    private val historyDb: HistoryRepository,
    private val userDataStore: UserDataStore,
    private val tokenDataStore: TokenDataStore,
    private val catalogDataStore: CatalogDataStore,
    private val appInfo: AppInfo,
    private val resolvePlaybackAction: ResolvePlaybackActionUseCase,
    private val recordPlaybackResult: RecordPlaybackResultUseCase
): ViewModel() {

    private val _event = MutableSharedFlow<CatalogEvent>()
    val event = _event.asSharedFlow()

    private val _showSortingSheet = MutableStateFlow(false)
    private val _showViewModeSheet = MutableStateFlow(false)

    private var hasLoadedContent = false

    private var currentMedia: Media? = null

    private val preferencesFlow = combine(
        historyDb.flow,
        catalogDataStore.flow,
        tokenDataStore.flow,
    ) { history, catalog, token  ->
        CatalogPreferences(
            history = history.toImmutableList(),
            sortingMode = catalog.sortingMode,
            viewMode = catalog.viewMode,
            token = token
        )
    }

    private val artworkFlow = combine(
        artworkDb.flowArtworks(),
        detailsDb.flowGenres()
    ) { artworks, genres ->
        artworks to genres.filterFor(artworks = artworks)
    }

    val uiState: StateFlow<CatalogUiState> = combine(
        artworkFlow,
        syncCatalogUseCase.state,
        preferencesFlow,
        _showSortingSheet,
        _showViewModeSheet
    ) { (artworks, genres), syncState, preferences, showSortingSheet, showViewModeSheet  ->

        if (syncState is SyncState.Syncing && (syncState.full || !hasLoadedContent)) {

            CatalogUiState(
                state = CatalogState.Loading(syncState = syncState),
            )

        } else {

            hasLoadedContent = true

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
                    sortingMode = preferences.sortingMode,
                    viewMode = preferences.viewMode,
                    showSortingSheet = showSortingSheet,
                    showViewSheet = showViewModeSheet
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
    }

    fun handleIntent(intent: CatalogIntent) = viewModelScope.launch {
        when (intent) {

            // Navigation
            is CatalogIntent.SyncCatalog -> syncCatalog()
            is CatalogIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is CatalogIntent.OnCategoryTap -> _event.emit(NavigateToSearch(category = intent.category))
            is CatalogIntent.OnGenreTap -> _event.emit(NavigateToSearch(genre = intent.genre))
            CatalogIntent.OnSearchTap -> _event.emit(NavigateToSearch())
            CatalogIntent.OnSettingsTap -> _event.emit(NavigateToSettings)
            CatalogIntent.OnHowToTap -> _event.emit(NavigateToHowTo)
            CatalogIntent.OnSourcesTap -> _event.emit(NavigateToSources)
            CatalogIntent.OnTokenTap -> _event.emit(NavigateToToken)

            // Sort
            is CatalogIntent.SelectSortingMode -> selectSortingOption(mode = intent.mode)
            is CatalogIntent.ShowSortingModes -> showSortingOptions(show = intent.show)

            // View mode
            is CatalogIntent.SelectViewMode -> selectViewMode(mode = intent.mode)
            is CatalogIntent.ShowViewModes -> showViewModes(show = intent.show)

            // History
            is CatalogIntent.DeleteHistoryEntry -> deleteHistoryEntry(entry = intent.entry)

            // Player
            is CatalogIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            is CatalogIntent.OnExternalPlayerResult -> onExternalPlayerResult(intent.progress)
        }
    }

    private suspend fun syncCatalog() {

        val fullSyncNeeded = UpdateManager.fullSyncIsNeeded(
            lastSyncVersionCode = userDataStore.getVersionCode(),
            currentVersionCode = appInfo.versionCode
        )

        if (fullSyncNeeded) {
            Trace.info("CatalogViewModel", "Full sync requested")
        }

        syncCatalogUseCase(onlyNew = !fullSyncNeeded)

    }

    private suspend fun onArtworkTap(artwork: Artwork, rgb: Int?) {

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

    private suspend fun deleteHistoryEntry(entry: HistoryEntry) {
        historyDb.delete(artworkId = entry.media.artworkId)
    }

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {
        currentMedia = media
        when (val action = resolvePlaybackAction(media = media, forceInternal = forceInternal)) {
            is PlaybackAction.OpenPlayer -> _event.emit(CatalogEvent.PlayMedia(
                media = action.media,
                externalPlayer = action.externalPlayer,
            ))
            PlaybackAction.Unavailable -> Unit
        }
    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        currentMedia?.let { recordPlaybackResult(media = it, progress = progress) }
    }

}