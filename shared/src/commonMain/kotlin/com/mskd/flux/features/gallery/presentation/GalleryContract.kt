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

/** The "Use as poster" / "Use as backdrop" button of the viewer, for the image shown. */
enum class ArtworkImageButton {
    /** The image cannot become a Custom image (viewer closed, or a Sync runs). */
    HIDDEN,
    /** "Use as poster" / "Use as backdrop": the image can become the Custom image of its type. */
    AVAILABLE,
    /** "Current poster" / "Current backdrop": the image is already the Poster or Backdrop shown for the Artwork. */
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
