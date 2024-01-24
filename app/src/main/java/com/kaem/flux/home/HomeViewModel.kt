package com.kaem.flux.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.model.flux.FluxArtwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val artworks: List<FluxArtwork> = emptyList()
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

        repository.getArtworks().collect { result ->

            _uiState.update {

                it.copy(
                    artworks = result.getOrNull().orEmpty(),
                    isLoading = false
                )

            }

        }

    }

}