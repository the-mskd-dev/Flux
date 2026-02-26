package com.kaem.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.repository.FirebaseRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.screens.home.HomeEvent.NavigateToArtwork
import com.kaem.flux.screens.home.HomeEvent.NavigateToCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val userRepository: UserRepository,
    private val firebaseRepository: FirebaseRepository
): ViewModel() {

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    private val _showMessage = MutableStateFlow(true)
    
    val uiState: StateFlow<HomeUiState> = combine(
        repository.catalogFlow,
        userRepository.flow,
        firebaseRepository.message,
        _showMessage
    ) { catalog, preferences, firebaseMessage, showMessage ->

        val screen = when {
            catalog.isLoading && catalog.artworks.isEmpty() -> ScreenState.LOADING
            else -> ScreenState.CONTENT
        }

        val message = firebaseMessage?.let {
            if (!preferences.watchedMessagesIds.contains(it.id) && showMessage)
                it
            else
                null
        }

        HomeUiState(
            screenState = screen,
            artworks = catalog.artworks,
            lastWatchedMediaIds = preferences.recentlyWatchedIds,
            isRefreshing = catalog.isLoading,
            message = message
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.OnSyncTap -> fetchCatalog(manualSync = intent.manualSync)
            is HomeIntent.OnArtworkTap -> _event.emit(NavigateToArtwork(mediaId = intent.artworkId))
            is HomeIntent.OnCategoryTap -> _event.emit(NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
            HomeIntent.OnPermissionTap -> _event.emit(HomeEvent.OpenPermissionDialog)
            HomeIntent.FetchMessages -> fetchMessages()
            HomeIntent.CloseMessage -> closeMessage()
            HomeIntent.DoNotShowMessage -> doNotShowMessage()
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

    private suspend fun fetchMessages() {
        firebaseRepository.fetchMessages()
    }

    private suspend fun closeMessage() {
        _showMessage.emit(false)
    }

    private suspend fun doNotShowMessage() {
        val message = uiState.first().message ?: return
        _showMessage.emit(false)
        userRepository.setMessageAsWatched(message.id)
    }

}