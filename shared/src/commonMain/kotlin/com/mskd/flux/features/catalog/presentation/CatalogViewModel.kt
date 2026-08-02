package com.mskd.flux.features.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.UpdateManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val database: DatabaseRepository,
    private val userDataStore: UserDataStore,
    private val tokenDataStore: TokenDataStore,
    private val appInfo: AppInfo
): ViewModel() {

    private val _event = MutableSharedFlow<CatalogEvent>()
    val event = _event.asSharedFlow()

    private var hasLoadedContent = false

    val uiState: StateFlow<CatalogUiState> = combine(
        database.flowArtworks(),
        syncCatalogUseCase.state,
        userDataStore.flow,
        tokenDataStore.flow,
    ) { artworks, catalogState, preferences, token ->

        if (catalogState is SyncState.Syncing && (catalogState.full || !hasLoadedContent)) {

            CatalogUiState(
                state = CatalogState.Loading(progress = catalogState.progress),
            )

        } else {

            hasLoadedContent = true

            CatalogUiState(
                state = CatalogState.Content(
                    artworks = artworks,
                    lastWatchedMediaIds = preferences.recentlyWatchedIds,
                    isRefreshing = catalogState is SyncState.Syncing,
                    tokenIsMissing = token.isBlank()
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
            is CatalogIntent.SyncCatalog -> syncCatalog()
            is CatalogIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is CatalogIntent.OnCategoryTap -> _event.emit(CatalogEvent.NavigateToCategory(category = intent.category))
            CatalogIntent.OnSearchTap -> _event.emit(CatalogEvent.NavigateToSearch)
            CatalogIntent.OnSettingsTap -> _event.emit(CatalogEvent.NavigateToSettings)
            CatalogIntent.OnHowToTap -> _event.emit(CatalogEvent.NavigateToHowTo)
            CatalogIntent.OnSourcesTap -> _event.emit(CatalogEvent.NavigateToSources)
            CatalogIntent.OnTokenTap -> _event.emit(CatalogEvent.NavigateToToken)
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
            artwork.id == Artwork.UNKNOWN_ID -> CatalogEvent.NavigateToUnknown
            artwork.type == ContentType.SHOW -> CatalogEvent.NavigateToShow(artworkId = artwork.id, rgb = rgb)
            else -> CatalogEvent.NavigateToMovie(artworkId = artwork.id, rgb = rgb)
        }

        _event.emit(event)

    }

}