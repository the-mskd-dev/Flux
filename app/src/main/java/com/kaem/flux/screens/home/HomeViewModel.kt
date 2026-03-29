package com.kaem.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.screens.home.HomeEvent.NavigateToArtwork
import com.kaem.flux.screens.home.HomeEvent.NavigateToCategory
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
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()
    
    val uiState: StateFlow<HomeUiState> = combine(
        repository.flow,
        userRepository.flow
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


    init {
        viewModelScope.launch {
            syncCatalog(manualSync = false)
        }
    }

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.SyncCatalog -> syncCatalog(manualSync = true)
            is HomeIntent.OnArtworkTap -> _event.emit(NavigateToArtwork(mediaId = intent.artworkId))
            is HomeIntent.OnCategoryTap -> _event.emit(NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
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

}