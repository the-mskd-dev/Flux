package com.kaem.flux.screens.category

import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: LibraryRepository
) : ViewModel() {

    val contentType: ContentType = ContentType.valueOf(checkNotNull(savedStateHandle["contentType"]))
    val overviews = repository.libraryFlow.value.artworkOverviews
        .filter { it.type == contentType }
        .sortedBy { it.title }


}