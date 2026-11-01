package com.mskd.flux.features.gallery.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.domain.model.GalleryImage

@Immutable
data class GalleryUiState(
    val title: String = "",
    val state: State<GalleryContent> = State.Loading,
    val failure: GalleryFailure? = null
)

enum class GalleryFailure { OFFLINE, ERROR }

@Immutable
data class GalleryContent(
    val images: List<GalleryImage>,
    val viewerIndex: Int? = null,
    val imageDownloadRunning: Boolean = false,
    val artworkImageButton: ArtworkImageButton = ArtworkImageButton.HIDDEN,
)

/** The "Utiliser cette image" button of the viewer, for the image shown. */
enum class ArtworkImageButton {
    /** The image cannot be the Artwork image (other type than the header shows, or a Synchro runs). */
    HIDDEN,
    /** "Utiliser cette image": the image can become the Image personnalisée. */
    AVAILABLE,
    /** "Image actuelle": the image is already the Artwork image. */
    CURRENT,
}

sealed interface GalleryIntent {
    data object OnBackTap : GalleryIntent
    data object OnRetryTap : GalleryIntent
    data class OnImageTap(val index: Int) : GalleryIntent
    data class OnViewerPageChange(val index: Int) : GalleryIntent
    data object OnViewerClose : GalleryIntent
    data object OnImageDownloadTap : GalleryIntent
    data object OnShowDownloadedImageTap : GalleryIntent
    data object OnUseImageTap : GalleryIntent
}

sealed interface GalleryEvent {
    data object BackToPreviousScreen : GalleryEvent
    data object ImageDownloadSucceeded : GalleryEvent
    data object ImageDownloadFailed : GalleryEvent
}
