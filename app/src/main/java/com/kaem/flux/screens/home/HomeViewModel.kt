package com.kaem.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.MediaOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private var lastSyncTime: Long = dataStoreRepository.getSyncTime()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.catalogFlow,
                dataStoreRepository.flow
            ) { catalogContent, preferences ->

                val screenState = when (_uiState.value.screenState) {
                    ScreenState.LOADING -> if (catalogContent.isLoading) ScreenState.LOADING else ScreenState.CONTENT
                    else -> ScreenState.CONTENT
                }

                HomeUiState(
                    screenState = screenState,
                    overviews = catalogContent.mediaOverviews,
                    lastWatchedMediaIds = preferences.watchedIds,
                    isRefreshing = catalogContent.isLoading
                )

            }.collect { _uiState.value = it }
        }
    }

    fun handleIntent(intent: HomeIntent) = viewModelScope.launch {
        when (intent) {
            is HomeIntent.OnSyncTap -> fetchCatalog(manualSync = intent.manualSync)
            is HomeIntent.OnMediaTap -> _event.emit(HomeEvent.NavigateToMedia(mediaId = intent.mediaId))
            is HomeIntent.OnCategoryTap -> _event.emit(HomeEvent.NavigateToCategory(category = intent.category))
            HomeIntent.OnSearchTap -> _event.emit(HomeEvent.NavigateToSearch)
            HomeIntent.OnSettingsTap -> _event.emit(HomeEvent.NavigateToSettings)
            HomeIntent.OnHowToTap -> _event.emit(HomeEvent.NavigateToHowTo)
            HomeIntent.OnPermissionTap -> _event.emit(HomeEvent.OpenPermissionDialog)
        }
    }

    private suspend fun fetchCatalog(manualSync: Boolean = false) {

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds || manualSync

        _uiState.value = _uiState.value.copy(
            isRefreshing = manualSync,
            screenState = if (manualSync && _uiState.value.overviews.isEmpty()) ScreenState.LOADING else _uiState.value.screenState
        )

        Log.i("LibraryViewModel", "getLibrary, sync : $sync")

        repository.getCatalog(sync)

        if (sync) {
            dataStoreRepository.setSyncTime(currentTime)
            lastSyncTime = currentTime
        }
    }

}