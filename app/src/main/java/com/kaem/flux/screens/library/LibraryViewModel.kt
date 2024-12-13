package com.kaem.flux.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.ArtworkOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

data class LibraryUiState(
    val artworkOverviews: List<ArtworkOverview> = emptyList(),
    val lastWatchedArtworkIds: List<Long> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private var lastSyncTime: Long = dataStoreRepository.getSyncTime()
    private val dataStorePreferencesFlow = dataStoreRepository.preferencesFlow

    private val libraryUiStateFlow = combine(
        repository.libraryContent,
        dataStorePreferencesFlow
    ) { libraryContent, preferences ->

        return@combine libraryContent?.let {
            LibraryUiState(
                artworkOverviews = libraryContent.artworkOverviews,
                lastWatchedArtworkIds = preferences.lastWatchedIds,
                isLoading = libraryContent.isLoading
            )
        }

    }
    val libraryUiState = libraryUiStateFlow.asLiveData()

    fun getLibrary() = viewModelScope.launch {

        val currentTime = System.currentTimeMillis()
        val sync = currentTime - lastSyncTime > 7.days.inWholeMilliseconds

        repository.getLibrary(sync)

        if (sync) {
            dataStoreRepository.saveSyncTime(currentTime)
            lastSyncTime = currentTime
        }
    }

}