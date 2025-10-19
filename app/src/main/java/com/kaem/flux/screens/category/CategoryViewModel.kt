package com.kaem.flux.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.artwork.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CatalogRepository
) : ViewModel() {

    val contentType: ContentType = ContentType.valueOf(checkNotNull(savedStateHandle["contentType"]))
    val overviews = repository.catalogFlow.value.artworkOverviews
        .filter { it.type == contentType }
        .sortedBy { it.title }

}