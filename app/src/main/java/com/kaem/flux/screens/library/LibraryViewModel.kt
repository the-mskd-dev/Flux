package com.kaem.flux.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.ArtworkOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

data class LibraryUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val artworkOverviews: List<ArtworkOverview> = emptyList(),
    val lastWatchedArtworkIds: List<Long> = emptyList(),
    val isSyncing: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private var lastSyncTime: Long = dataStoreRepository.getSyncTime()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.libraryFlow,
                dataStoreRepository.preferencesFlow
            ) { libraryContent, preferences ->

                val screenState = when {
                    _uiState.value.screenState == ScreenState.LOADING -> {
                        if (libraryContent.isLoading) ScreenState.LOADING else ScreenState.CONTENT
                    }
                    else -> ScreenState.CONTENT
                }

                LibraryUiState(
                    screenState = screenState,
                    artworkOverviews = libraryContent.artworkOverviews,
                    lastWatchedArtworkIds = preferences.lastWatchedIds,
                    isSyncing = libraryContent.isLoading
                )
            }.collect { _uiState.value = it }
        }
    }

    fun getLibrary(manualSync: Boolean = false) = viewModelScope.launch {

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 1.days.inWholeMilliseconds || manualSync

        _uiState.update {
            it.copy(isSyncing = manualSync)
        }

        Log.i("LibraryViewModel", "getLibrary, sync : $sync")

        repository.getLibrary(sync)

        if (sync) {
            dataStoreRepository.saveSyncTime(currentTime)
            lastSyncTime = currentTime
        }
    }



}