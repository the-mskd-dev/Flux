package com.kaem.flux.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.MediaOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

data class HomeUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val overviews: List<MediaOverview> = emptyList(),
    val lastWatchedMediaIds: List<Long> = emptyList(),
    val isSyncing: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private var lastSyncTime: Long = dataStoreRepository.getSyncTime()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.catalogFlow,
                dataStoreRepository.flow
            ) { libraryContent, preferences ->

                val screenState = when {
                    _uiState.value.screenState == ScreenState.LOADING -> {
                        if (libraryContent.isLoading) ScreenState.LOADING else ScreenState.CONTENT
                    }
                    else -> ScreenState.CONTENT
                }

                HomeUiState(
                    screenState = screenState,
                    overviews = libraryContent.mediaOverviews,
                    lastWatchedMediaIds = preferences.watchedIds,
                    isSyncing = libraryContent.isLoading
                )
            }.collect { _uiState.value = it }
        }
    }

    fun getLibrary(manualSync: Boolean = false) = viewModelScope.launch {

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds || manualSync

        _uiState.value = _uiState.value.copy(
            isSyncing = manualSync,
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