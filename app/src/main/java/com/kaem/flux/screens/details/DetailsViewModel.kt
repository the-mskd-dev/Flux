package com.kaem.flux.screens.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxShow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class DetailsUiState(
    val artwork: FluxArtwork,
    val episodes: List<FluxEpisode>,
    val selectedEpisode: FluxEpisode?
)

@HiltViewModel
class DetailsViewModel @Inject constructor(private val repository: LibraryRepository) : ViewModel() {

    var uiState by mutableStateOf<DetailsUiState?>(null)
        private set

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