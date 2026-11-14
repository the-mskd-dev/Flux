package com.mskd.flux.features.gallery.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType

@Immutable
data class GalleryUiState(
    val title: String = "",
    val state: State<GalleryContent> = State.Loading,
    val failure: GalleryFailure? = null
)

enum class GalleryFailure { OFFLINE, ERROR }

@Immutable
data class GalleryContent(
    val allImages: List<GalleryImage>,
    val selectedTypes: Set<GalleryImageType> = emptySet(),
    val filterSheetVisible: Boolean = false,
    val viewerIndex: Int? = null,
    val imageDownloadRunning: Boolean = false,
    val artworkImageButton: ArtworkImageButton = ArtworkImageButton.HIDDEN,
) {
    /** The images shown by the grid and the viewer: nothing selected means every image. */
    val images: List<GalleryImage> =
        if (selectedTypes.isEmpty()) allImages else allImages.filter { it.type in selectedTypes }
}

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
    data object OnFilterTap : GalleryIntent
    data object OnFilterDismiss : GalleryIntent
    data class OnFilterTypeTap(val type: GalleryImageType) : GalleryIntent
    data object OnFilterClearTap : GalleryIntent
}

sealed interface GalleryEvent {
    data object BackToPreviousScreen : GalleryEvent
    data object ImageDownloadSucceeded : GalleryEvent
    data object ImageDownloadFailed : GalleryEvent
}
