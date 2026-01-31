package com.kaem.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()
    
    val uiState: StateFlow<HomeUiState> = combine(
        repository.catalogFlow,
        userRepository.flow,
    ) { catalog, preferences ->

        val screen = when {
            catalog.isLoading && catalog.artworks.isEmpty() -> ScreenState.LOADING
            else -> ScreenState.CONTENT
        }

        HomeUiState(
            screenState = screen,
            artworks = catalog.artworks,
            lastWatchedMediaIds = preferences.recentlyWatchedIds,
            isRefreshing = catalog.isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.OnSyncTap -> fetchCatalog(manualSync = intent.manualSync)
            is HomeIntent.OnArtworkTap -> _event.emit(HomeEvent.NavigateToArtwork(mediaId = intent.artworkId))
            is HomeIntent.OnCategoryTap -> _event.emit(HomeEvent.NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
            HomeIntent.OnPermissionTap -> _event.emit(HomeEvent.OpenPermissionDialog)
        }
    }

    private suspend fun fetchCatalog(manualSync: Boolean = false) {

        val lastSyncTime = userRepository.getSyncTime()

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds || manualSync

        Log.i("LibraryViewModel", "getLibrary, sync : $sync")

        repository.getCatalog(sync)

        if (sync) {
            userRepository.setSyncTime(currentTime)
        }

    }

}