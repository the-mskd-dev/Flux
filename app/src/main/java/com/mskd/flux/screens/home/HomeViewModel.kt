package com.mskd.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.tmdb.token.TokenProvider
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.screens.home.HomeEvent.NavigateToArtwork
import com.mskd.flux.screens.home.HomeEvent.NavigateToCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    private val _snackbarState = MutableStateFlow(HomeUiState.SnackbarState())
    
    val uiState: StateFlow<HomeUiState> = combine(
        repository.flow,
        userRepository.flow,
        _snackbarState
    ) { catalog, preferences, snackbar ->

        val screen = when {
            catalog.isLoading && catalog.artworks.isEmpty() -> ScreenState.LOADING
            else -> ScreenState.CONTENT
        }

        HomeUiState(
            screenState = screen,
            artworks = catalog.artworks,
            lastWatchedMediaIds = preferences.recentlyWatchedIds,
            isRefreshing = catalog.isLoading,
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

        viewModelScope.launch {
            if (tokenProvider.getToken().isBlank())
                _snackbarState.update { it.copy(show = true) }
        }
    }

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.SyncCatalog -> syncCatalog(manualSync = true)
            is HomeIntent.OnArtworkTap -> onArtworkTap(artworkId = intent.artworkId)
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

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds || manualSync

        if (sync) {

            Log.i("HomeViewModel", "syncCatalog, catalog sync requested")

            repository.syncCatalog()
            userRepository.setSyncTime(currentTime)

        } else {

            Log.i("HomeViewModel", "syncCatalog, catalog sync not needed")

        }

    }

    private suspend fun onArtworkTap(artworkId: Long) {

        val event = when (artworkId) {
            Artwork.UNKNOWN_ID -> HomeEvent.NavigateToUnknown
            else -> NavigateToArtwork(artworkId = artworkId)
        }

        _event.emit(event)

    }

    private suspend fun onSnackbarActionTap() {
        _snackbarState.update { it.copy(show = false) }
        _event.emit(HomeEvent.NavigateToToken)
    }

    private fun onDismissSnackbar() {
        _snackbarState.update { it.copy(show = false) }
    }

}