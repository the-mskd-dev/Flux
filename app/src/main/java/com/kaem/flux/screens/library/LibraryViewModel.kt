package com.kaem.flux.screens.library

import android.icu.util.TimeUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.sql.Time
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

data class LibraryUiState(
    val artworks: List<Artwork> = emptyList(),
    val lastWatchedArtworkIds: List<Long> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private var lastSyncTime: Long = 0
    private val dataStorePreferencesFlow = dataStoreRepository.preferencesFlow

    private val libraryUiStateFlow = combine(
        repository.libraryContent,
        dataStorePreferencesFlow
    ) { libraryContent, preferences ->

        lastSyncTime = preferences.lastSyncTime

        return@combine libraryContent?.let {
            LibraryUiState(
                artworks = libraryContent.artworks,
                lastWatchedArtworkIds = preferences.lastWatchedIds,
                isLoading = libraryContent.isLoading
            )
        }

    }
    val libraryUiState = libraryUiStateFlow.asLiveData()

    fun getLibrary() = viewModelScope.launch {

        val sync = System.currentTimeMillis() - lastSyncTime > 7.days.inWholeMilliseconds

        repository.getLibrary(sync)

        if (sync)
            dataStoreRepository.saveSyncTime(System.currentTimeMillis())
    }

}