package com.mskd.flux.features.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<GalleryEvent>()
    val event: SharedFlow<GalleryEvent> = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

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
            GalleryIntent.OnRetryTap -> loadImages()
        }
    }

    //endregion

    //region Private Methods

    private fun loadTitle() = viewModelScope.launch {
        val title = databaseRepository.getArtwork(artworkId = artworkId)?.title ?: return@launch
        _uiState.update { it.copy(title = title) }
    }

    private fun loadImages() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { fetchImages() }
    }

    private suspend fun fetchImages() {

        val hasContent = _uiState.value.state is State.Content

        // Keep the images already displayed visible while retrying
        _uiState.update { if (hasContent) it.copy(failure = null) else it.copy(state = State.Loading, failure = null) }

        if (!connectivityRepository.currentlyOnline()) {
            fail(GalleryFailure.OFFLINE)
            return
        }

        val images = try {
            getGalleryImages(artworkId = artworkId, contentType = contentType)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Trace.error("GalleryViewModel", "Fail to get images for artworkId:$artworkId", e)
            fail(if (connectivityRepository.currentlyOnline()) GalleryFailure.ERROR else GalleryFailure.OFFLINE)
            return
        }

        _uiState.update { it.copy(state = State.Content(GalleryContent(images = images)), failure = null) }

    }

    // Without displayed images the whole page is in error, otherwise the images stay and only the failure is flagged
    private fun fail(failure: GalleryFailure) = _uiState.update {
        if (it.state is State.Content) it.copy(failure = failure) else it.copy(state = State.Error(), failure = failure)
    }

    //endregion

}
