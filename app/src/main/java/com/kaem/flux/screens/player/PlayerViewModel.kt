package com.kaem.flux.screens.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.WatchTime
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkContent
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.FluxStatus
import com.kaem.flux.model.flux.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val artwork: Artwork = Artwork(),
    val artworkInfo: ArtworkInfo? = null
) {

    val path = artworkInfo?.file?.path ?: ""

    val currentTime = artworkInfo?.currentTime ?: 0L
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

        val artworkInfo = when (artwork.content) {
            is ArtworkContent.MOVIE -> artwork.content.movie
            is ArtworkContent.SHOW -> artwork.content.episodes.find { it.id == episodeId }
        }

        _uiState.value = PlayerUiState(
            artwork = artwork,
            artworkInfo = artworkInfo
        )

    }

    fun saveCurrentTime(time: Long) = viewModelScope.launch {

        uiState.value.let { state ->

            state.artworkInfo?.let { artworkInfo ->
                artworkInfo.currentTime = time
                val watchTime = WatchTime.fromTime(time)
                artworkInfo.status = if (watchTime.timeInMin >= artworkInfo.duration) FluxStatus.WATCHED else FluxStatus.IS_WATCHING
            }

            when (state.artworkInfo) {

                is Movie -> repository.saveArtwork(state.artwork)

                is Episode -> repository.saveEpisodes(listOf(state.artworkInfo))

                else -> {}

            }

        }

    }

}