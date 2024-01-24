package com.kaem.flux.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.tmdb.TMDBClient
import com.kaem.flux.data.tmdb.TMDBService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val shows: List<String> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
): ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refreshFiles() }

    fun refreshFiles() = viewModelScope.launch {

        _uiState.update {

            it.copy(isLoading = true)

        }

        repository.getLocalFiles().collect { result ->

            _uiState.update {

                it.copy(
                    shows = (result.getOrNull() ?: emptyList()).map { it.name },
                    isLoading = false
                )

            }

        }

    }

    fun test() = viewModelScope.launch {

        val result = TMDBClient.service.authenticate()

        Log.d("TEST", "authentication : ${result.success}")

        val naruto = TMDBClient.service.search(name = "naruto")

        Log.d("TEST", "search : ${naruto.results.size}")

        val episodeNaruto = TMDBClient.service.episode(
            seriesId = 46260,
            season = 1,
            episode = 1
        )

        Log.d("TEST", "episode : $episodeNaruto")

    }
}