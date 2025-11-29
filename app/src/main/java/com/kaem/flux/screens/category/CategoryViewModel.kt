package com.kaem.flux.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.model.media.ContentType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted val contentType: ContentType,
    repository: CatalogRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(contentType: ContentType): CategoryViewModel
    }

    private val _event = MutableSharedFlow<CategoryEvent>()
    val event = _event.asSharedFlow()

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