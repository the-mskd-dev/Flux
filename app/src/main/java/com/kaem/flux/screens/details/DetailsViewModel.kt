package com.kaem.flux.screens.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.model.flux.FluxStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class DetailsUiState(
    val artwork: FluxArtwork,
    val episodes: List<FluxEpisode>,
    val selectedEpisode: FluxEpisode?
) {

    val currentEpisode: FluxEpisode? = episodes.lastOrNull { it.status == FluxStatus.IS_WATCHING }
        ?: episodes.firstOrNull { it.status == FluxStatus.NOT_WATCHED }
        ?: episodes.firstOrNull()

    val description: String? = when (artwork) {
        is FluxShow -> currentEpisode?.description
        is FluxMovie -> artwork.description
        else -> null
    }

}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["artworkId"])

    var uiState by mutableStateOf<DetailsUiState?>(null)
        private set

    init {

        getArtworks(artworkId)

    }

    fun getArtworks(id: Int) {

        val libraryContent = repository.libraryContent.value

        val artwork = libraryContent?.artworks?.find { it.id == id } ?: return
        val episodes = if (artwork is FluxShow)  libraryContent.episodes.filter { it.showId == id } else emptyList()

        uiState = DetailsUiState(
            artwork = artwork,
            episodes = episodes,
            selectedEpisode = episodes.firstOrNull()
        )

    }

}