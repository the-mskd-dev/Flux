package com.mskd.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.model.AppInfo
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.screens.home.HomeEvent.NavigateToArtwork
import com.mskd.flux.screens.home.HomeEvent.NavigateToCategory
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.utils.FluxSnackbar
import com.mskd.flux.utils.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalogUC: CatalogUC,
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val snackbarRepository: SnackbarRepository,
    private val appInfo: AppInfo
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    private val _dismissedSnackbar = MutableStateFlow<Set<FluxSnackbar>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        catalogUC.artworks,
        catalogUC.state,
        userRepository.flow,
        tokenRepository.flow,
        _dismissedSnackbar,
    ) { artworks, catalogState, preferences, token, dismissedSnackbar ->

        val screen = when {
            catalogState is CatalogUC.State.Syncing -> {
                if (catalogState.full) HomeUiState.State.Loading(progress = catalogState.progress)
                else HomeUiState.State.Content
            }
            else -> HomeUiState.State.Content
        }

        val snackbar = getSnackbarIfNeeded(
            token = token,
            dismissedSnackbar = dismissedSnackbar,
            artworks = artworks
        )

        HomeUiState(
            screenState = screen,
            artworks = artworks,
            lastWatchedMediaIds = preferences.recentlyWatchedIds,
            isRefreshing = catalogState is CatalogUC.State.Syncing,
            snackbarState = snackbar
        )

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
            is HomeIntent.OnArtworkTap -> onArtworkTap(artworkId = intent.artworkId, rgb = intent.rgb)
            is HomeIntent.OnCategoryTap -> _event.emit(NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSnackbarActionTap -> onSnackbarActionTap()
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
            HomeIntent.OnDismissSnackbar -> onDismissSnackbar()
        }
    }

    private suspend fun syncCatalog(manualSync: Boolean = false) {

        val lastSyncTime = userRepository.getSyncTime()
        val lastSyncVersionCode = userRepository.getVersionCode()

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds
                || manualSync
                || lastSyncVersionCode < appInfo.versionCode

        if (sync) {

            Log.i("HomeViewModel", "syncCatalog, catalog sync requested")

            val fullSyncNeeded = UpdateManager.fullSyncIsNeeded(
                lastSyncVersionCode = lastSyncVersionCode,
                currentVersionCode = appInfo.versionCode
            )

            catalogUC.syncCatalog(onlyNew = !fullSyncNeeded)

        } else {

            catalogUC.cleanCatalog()
            Log.i("HomeViewModel", "syncCatalog, catalog sync not needed")

        }

    }

    private suspend fun onArtworkTap(artworkId: Long, rgb: Int?) {

        val event = when (artworkId) {
            Artwork.UNKNOWN_ID -> HomeEvent.NavigateToUnknown
            else -> NavigateToArtwork(artworkId = artworkId, rgb = rgb)
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
        _dismissedSnackbar.update { it + (uiState.value.snackbarState ?: return) }
    }

    private suspend fun getSnackbarIfNeeded(
        token: String,
        dismissedSnackbar: Set<FluxSnackbar>,
        artworks: List<Artwork>,
    ) : FluxSnackbar? {

        return when {
            token.isBlank()
                    && dismissedSnackbar.contains(FluxSnackbar.Token).not()
                    && snackbarRepository.canShow(FluxSnackbar.Token.id).first() -> {

                snackbarRepository.incrementCount(FluxSnackbar.Token.id)
                FluxSnackbar.Token

            }
            token.isNotBlank()
                    && artworks.any { it.id == Artwork.UNKNOWN_ID }
                    && dismissedSnackbar.contains(FluxSnackbar.Tutorial).not()
                    && snackbarRepository.canShow(FluxSnackbar.Tutorial.id).first() -> {

                snackbarRepository.incrementCount(FluxSnackbar.Tutorial.id)
                FluxSnackbar.Tutorial

            }
            else -> null
        }

    }

}