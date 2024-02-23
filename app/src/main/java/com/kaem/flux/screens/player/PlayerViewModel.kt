package com.kaem.flux.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkContent
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.screens.details.ArtworkUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PlayerUiState(
    val path: String = ""
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["id"])
    private val episodeId: Int? = savedStateHandle["episodeId"]

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {

        val libraryContent = repository.libraryContent.value

        libraryContent?.artworks?.find { it.id == artworkId }?.let { artwork ->

            val path = when (artwork.content) {

                is ArtworkContent.MOVIE -> artwork.content.movie.file.path
                is ArtworkContent.SHOW -> {

                    artwork.content.episodes.first { it.id == episodeId!! }.file.path

                }

            }

            _uiState.value = PlayerUiState(path = path)

        }

    }

}