package com.kaem.flux.screens.details

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.flux.FluxArtworkSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(private val repository: LibraryRepository) : ViewModel() {

    init {

        val artworks = repository.libraryContent.value.artworks
        Log.d("TEST", "artworks : $artworks")

    }

    fun getArtworks(id: Int) : FluxArtworkSummary? {
        return repository.libraryContent.value.artworks.find { it.id == id }
    }

}