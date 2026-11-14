package com.mskd.flux.features.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.shownBackdropPath
import com.mskd.flux.core.model.artwork.shownLogoPath
import com.mskd.flux.core.model.artwork.shownPosterPath
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.features.gallery.domain.usecase.ImageDownloadUseCase
import com.mskd.flux.report.Trace
import com.mskd.flux.system.FilesLauncher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val artworkId: Long,
    private val contentType: ContentType,
    private val getGalleryImages: GetGalleryImagesUseCase,
    private val databaseRepository: DatabaseRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val imageDownload: ImageDownloadUseCase,
    private val filesLauncher: FilesLauncher,
    catalogSyncCoordinator: CatalogSyncCoordinator,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<GalleryEvent>()
    val event: SharedFlow<GalleryEvent> = _event.asSharedFlow()

    // The state driven by the user and the images source; the observed Artwork completes it in [uiState]
    private val _uiState = MutableStateFlow(GalleryUiState())

    private val syncing = catalogSyncCoordinator.state.map { it is SyncState.Syncing }

    val uiState: StateFlow<GalleryUiState> = combine(
        _uiState,
        databaseRepository.flowArtwork(artworkId = artworkId),
        syncing,
    ) { ui, artwork, syncing ->
        val content = (ui.state as? State.Content)?.content
        ui.copy(
            title = artwork?.title.orEmpty(),
            state = if (content == null) ui.state else State.Content(
                content.copy(artworkImageButton = artworkImageButton(content.viewedImage(), artwork, syncing))
            )
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = GalleryUiState())

    private var loadJob: Job? = null

    //endregion

    //region Init

    init {
        loadImages()
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.OnBackTap ->
                if (viewerIndex() != null) setViewerIndex(null)
                else viewModelScope.launch { _event.emit(GalleryEvent.BackToPreviousScreen) }
            GalleryIntent.OnRetryTap -> loadImages()
            is GalleryIntent.OnImageTap -> setViewerIndex(intent.index)
            is GalleryIntent.OnViewerPageChange -> if (viewerIndex() != null) setViewerIndex(intent.index)
            GalleryIntent.OnViewerClose -> setViewerIndex(null)
            GalleryIntent.OnImageDownloadTap -> downloadViewedImage()
            GalleryIntent.OnShowDownloadedImageTap -> filesLauncher.openDownloadedImages()
            GalleryIntent.OnUseImageTap -> useViewedImage()
            GalleryIntent.OnFilterTap -> setFilterSheetVisible(true)
            GalleryIntent.OnFilterDismiss -> setFilterSheetVisible(false)
            is GalleryIntent.OnFilterTypeTap -> toggleFilterType(intent.type)
            GalleryIntent.OnFilterClearTap -> clearFilterTypes()
        }
    }

    //endregion

    //region Private Methods

    // The filter is locked while the viewer is open, so the viewer index never needs to follow a change of the filtered list
    private fun updateFilter(transform: (GalleryContent) -> GalleryContent) = _uiState.update { ui ->
        val content = (ui.state as? State.Content)?.content ?: return@update ui
        if (content.viewerIndex != null) return@update ui
        ui.copy(state = State.Content(transform(content)))
    }

    private fun setFilterSheetVisible(visible: Boolean) = updateFilter { it.copy(filterSheetVisible = visible) }

    private fun toggleFilterType(type: GalleryImageType) = updateFilter {
        it.copy(selectedTypes = if (type in it.selectedTypes) it.selectedTypes - type else it.selectedTypes + type)
    }

    private fun clearFilterTypes() = updateFilter { it.copy(selectedTypes = emptySet()) }

    private fun viewerIndex(): Int? = (_uiState.value.state as? State.Content)?.content?.viewerIndex

    // Ignores indexes outside the list, null closes the viewer
    private fun setViewerIndex(index: Int?) = _uiState.update { ui ->
        val content = (ui.state as? State.Content)?.content ?: return@update ui
        if (index != null && index !in content.images.indices) return@update ui
        ui.copy(state = State.Content(content.copy(viewerIndex = index)))
    }

    // Runs in the view model scope so that swiping to another page does not cancel it
    private fun downloadViewedImage() {

        val content = (_uiState.value.state as? State.Content)?.content ?: return
        if (content.imageDownloadRunning) return
        val image = content.viewedImage() ?: return

        setImageDownloadRunning(true)

        viewModelScope.launch {
            try {
                imageDownload(image = image, artworkTitle = uiState.value.title)
                _event.emit(GalleryEvent.ImageDownloadSucceeded)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error("GalleryViewModel", "Fail to download image ${image.path} of artworkId:$artworkId", e)
                _event.emit(GalleryEvent.ImageDownloadFailed)
            } finally {
                setImageDownloadRunning(false)
            }
        }

    }

    private fun setImageDownloadRunning(running: Boolean) = _uiState.update { ui ->
        val content = (ui.state as? State.Content)?.content ?: return@update ui
        ui.copy(state = State.Content(content.copy(imageDownloadRunning = running)))
    }

    private fun GalleryContent.viewedImage(): GalleryImage? = viewerIndex?.let { images.getOrNull(it) }

    // Same rule as the page header and the Catalog, so that "Current poster" / "Current backdrop" / "Current logo" is always the image they show
    private fun artworkImageButton(image: GalleryImage?, artwork: Artwork?, syncing: Boolean): ArtworkImageButton = when {
        image == null || artwork == null || syncing -> ArtworkImageButton.HIDDEN
        image.path == artwork.shownPathOf(image.type) -> ArtworkImageButton.CURRENT
        else -> ArtworkImageButton.AVAILABLE
    }

    private fun Artwork.shownPathOf(type: GalleryImageType): String = when (type) {
        GalleryImageType.BACKDROP -> shownBackdropPath
        GalleryImageType.POSTER -> shownPosterPath
        GalleryImageType.LOGO -> shownLogoPath.orEmpty()
    }

    // The Artwork is observed: the button becomes "Current poster" / "Current backdrop" by itself once saved
    private fun useViewedImage() {

        val content = (uiState.value.state as? State.Content)?.content ?: return
        if (content.artworkImageButton != ArtworkImageButton.AVAILABLE) return
        val image = content.viewedImage() ?: return

        viewModelScope.launch {
            try {
                when (image.type) {
                    GalleryImageType.BACKDROP -> databaseRepository.setArtworkCustomBackdrop(artworkId = artworkId, path = image.path)
                    GalleryImageType.POSTER -> databaseRepository.setArtworkCustomPoster(artworkId = artworkId, path = image.path)
                    GalleryImageType.LOGO -> databaseRepository.setArtworkCustomLogo(artworkId = artworkId, path = image.path)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error("GalleryViewModel", "Fail to use image ${image.path} as custom ${image.type} of artworkId:$artworkId", e)
            }
        }

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

        _uiState.update {
            // Keep the viewer open on the same image when a retry reloads the list, close it if the image is gone
            val previous = (it.state as? State.Content)?.content
            val viewed = previous?.viewerIndex?.let { index -> previous.images.getOrNull(index) }
            val running = previous?.imageDownloadRunning ?: false
            val content = GalleryContent(
                allImages = images,
                selectedTypes = previous?.selectedTypes.orEmpty(),
                filterSheetVisible = previous?.filterSheetVisible ?: false,
                imageDownloadRunning = running,
            )
            // Index in the filtered list
            val viewerIndex = viewed?.let { image -> content.images.indexOfFirst { it.type == image.type && it.path == image.path } }
                ?.takeIf { index -> index >= 0 }
            it.copy(
                state = State.Content(content.copy(viewerIndex = viewerIndex)),
                failure = null
            )
        }

    }

    // Without displayed images the whole page is in error, otherwise the images stay and only the failure is flagged
    private fun fail(failure: GalleryFailure) = _uiState.update {
        if (it.state is State.Content) it.copy(failure = failure) else it.copy(state = State.Error(), failure = failure)
    }

    //endregion

}
