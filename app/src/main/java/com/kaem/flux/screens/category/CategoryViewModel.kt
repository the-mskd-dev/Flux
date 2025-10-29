package com.kaem.flux.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.media.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CatalogRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<CategoryEvent>()
    val event = _event.asSharedFlow()

    val contentType: ContentType = ContentType.valueOf(checkNotNull(savedStateHandle["contentType"]))
    val overviews = repository.catalogFlow.value.mediaOverviews
        .filter { it.type == contentType }
        .sortedBy { it.title }

    fun handleIntent(intent: CategoryIntent) = viewModelScope.launch {
        when (intent) {
            is CategoryIntent.OnBackTap -> _event.emit(CategoryEvent.BackToPreviousScreen)
            is CategoryIntent.OnMediaTap -> _event.emit(CategoryEvent.NavigateToMedia(intent.mediaId))
        }
    }

}