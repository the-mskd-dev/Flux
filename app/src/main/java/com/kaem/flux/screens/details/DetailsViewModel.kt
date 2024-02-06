package com.kaem.flux.screens.details

import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtwork
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class DetailsUiState(
    val id: Int,
    val artworkSummary: FluxArtwork
)

@HiltViewModel
class DetailsViewModel @Inject constructor(private val repository: LibraryRepository) : ViewModel() {

    fun getArtworks(id: Int) : FluxArtwork? {
        return repository.libraryContent.value?.artworks?.find { it.id == id }
    }

}