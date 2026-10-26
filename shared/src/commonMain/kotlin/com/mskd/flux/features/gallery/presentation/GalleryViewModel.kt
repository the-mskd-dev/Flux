package com.mskd.flux.features.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.report.Trace
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val artworkId: Long,
    private val contentType: ContentType,
    private val getGalleryImages: GetGalleryImagesUseCase,
    private val databaseRepository: DatabaseRepository,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<GalleryEvent>()
    val event: SharedFlow<GalleryEvent> = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    //endregion

    //region Init

    init {
        loadTitle()
        loadImages()
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: GalleryIntent) = viewModelScope.launch {
        when (intent) {
            GalleryIntent.OnBackTap -> _event.emit(GalleryEvent.BackToPreviousScreen)
        }
    }

    //endregion

    //region Private Methods

    private fun loadTitle() = viewModelScope.launch {
        val title = databaseRepository.getArtwork(artworkId = artworkId)?.title ?: return@launch
        _uiState.update { it.copy(title = title) }
    }

    private fun loadImages() = viewModelScope.launch {

        _uiState.update { it.copy(state = State.Loading) }

        val images = try {
            getGalleryImages(artworkId = artworkId, contentType = contentType)
        } catch (e: Exception) {
            Trace.error("GalleryViewModel", "Fail to get images for artworkId:$artworkId", e)
            _uiState.update { it.copy(state = State.Error()) }
            return@launch
        }

        _uiState.update { it.copy(state = State.Content(GalleryContent(images = images))) }

    }

    //endregion

}
