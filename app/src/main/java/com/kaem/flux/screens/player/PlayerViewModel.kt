package com.kaem.flux.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkContent
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PlayerUiState(
    val artwork: Artwork = Artwork(),
    val episode: Episode? = null
) {

    val isMovie = episode == null

    val path = episode?.file?.path ?: (artwork.content as? ArtworkContent.MOVIE)?.movie?.file?.path ?: ""

    val currentTime = episode?.currentTime ?: (artwork.content as? ArtworkContent.MOVIE)?.movie?.currentTime ?: 0L
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["id"])
    private val episodeId: Int = checkNotNull(savedStateHandle["episodeId"])

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {

        val libraryContent = repository.libraryContent.value

        val artwork = libraryContent!!.artworks.first { it.id == artworkId }
        val episode = (artwork.content as? ArtworkContent.SHOW)?.episodes?.first { it.id == episodeId }

        _uiState.value = PlayerUiState(
            artwork = artwork,
            episode = episode
        )

    }

    fun saveCurrentTime(time: Long) {

    }

}