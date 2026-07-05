package com.mskd.flux.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.datastore.SnackbarDataStore
import com.mskd.flux.core.data.datastore.TokenDataStore
import com.mskd.flux.core.data.datastore.UserDataStore
import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.utils.FluxSnackbar
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.UpdateManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

class HomeViewModel(
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val cleanCatalogUseCase: CleanCatalogUseCase,
    private val database: DatabaseRepository,
    private val userDataStore: UserDataStore,
    private val tokenDataStore: TokenDataStore,
    private val snackbarDataStore: SnackbarDataStore,
    private val appInfo: AppInfo
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    private val _dismissedSnackbar = MutableStateFlow<Set<FluxSnackbar>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        database.flowArtworks(),
        syncCatalogUseCase.state,
        userDataStore.flow,
        tokenDataStore.flow,
        _dismissedSnackbar,
    ) { artworks, catalogState, preferences, token, dismissedSnackbar ->

        val snackbar = getSnackbarIfNeeded(
            token = token,
            dismissedSnackbar = dismissedSnackbar,
            artworks = artworks
        )

        if (catalogState is SyncState.Syncing && catalogState.full) {

            HomeUiState(
                state = HomeState.Loading(progress = catalogState.progress),
                snackbarState = snackbar
            )

        } else {

            HomeUiState(
                state = HomeState.Content(
                    artworks = artworks,
                    lastWatchedMediaIds = preferences.recentlyWatchedIds,
                    isRefreshing = catalogState is SyncState.Syncing,
                ),
                snackbarState = snackbar
            )

        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )


    init {
        viewModelScope.launch {
            syncCatalog(manualSync = false)
        }
    }

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.SyncCatalog -> syncCatalog(manualSync = true)
            is HomeIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is HomeIntent.OnCategoryTap -> _event.emit(HomeEvent.NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSnackbarActionTap -> onSnackbarActionTap()
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
            HomeIntent.OnDismissSnackbar -> onDismissSnackbar()
        }
    }

    private suspend fun syncCatalog(manualSync: Boolean = false) {

        val lastSyncTime = userDataStore.getSyncTime()
        val lastSyncVersionCode = userDataStore.getVersionCode()

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds
                || manualSync
                || lastSyncVersionCode < appInfo.versionCode

        if (sync) {

            Trace.info("HomeViewModel", "syncCatalog, catalog sync requested")

            val fullSyncNeeded = UpdateManager.fullSyncIsNeeded(
                lastSyncVersionCode = lastSyncVersionCode,
                currentVersionCode = appInfo.versionCode
            )

            syncCatalogUseCase(onlyNew = !fullSyncNeeded)

        } else {

            cleanCatalogUseCase()
            Trace.info("HomeViewModel", "syncCatalog, catalog sync not needed")

        }

    }

    private suspend fun onArtworkTap(artwork: Artwork, rgb: Int?) {

        val event = when {
            artwork.id == Artwork.UNKNOWN_ID -> HomeEvent.NavigateToUnknown
            artwork.type == ContentType.SHOW -> HomeEvent.NavigateToShow(artworkId = artwork.id, rgb = rgb)
            else -> HomeEvent.NavigateToMovie(artworkId = artwork.id, rgb = rgb)
        }

        _event.emit(event)

    }

    private suspend fun onSnackbarActionTap() {
        val snackbar = uiState.value.snackbarState ?: return
        _dismissedSnackbar.update { it + snackbar }

        when (snackbar) {
            FluxSnackbar.Token -> _event.emit(HomeEvent.NavigateToToken)
            FluxSnackbar.Tutorial -> _event.emit(HomeEvent.NavigateToHowTo)
        }

    }

    private fun onDismissSnackbar() {
        val snackbar = uiState.value.snackbarState ?: return
        _dismissedSnackbar.update { it + snackbar }
    }

    private suspend fun getSnackbarIfNeeded(
        token: String,
        dismissedSnackbar: Set<FluxSnackbar>,
        artworks: List<Artwork>,
    ) : FluxSnackbar? {

        return when {
            token.isBlank()
                    && dismissedSnackbar.contains(FluxSnackbar.Token).not()
                    && snackbarDataStore.canShow(FluxSnackbar.Token.id).first() -> {

                snackbarDataStore.incrementCount(FluxSnackbar.Token.id)
                FluxSnackbar.Token

            }
            token.isNotBlank()
                    && artworks.any { it.id == Artwork.UNKNOWN_ID }
                    && dismissedSnackbar.contains(FluxSnackbar.Tutorial).not()
                    && snackbarDataStore.canShow(FluxSnackbar.Tutorial.id).first() -> {

                snackbarDataStore.incrementCount(FluxSnackbar.Tutorial.id)
                FluxSnackbar.Tutorial

            }
            else -> null
        }

    }

}