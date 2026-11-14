package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.presentation.ArtworkImageButton
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.gallery_current_backdrop
import flux.shared.generated.resources.gallery_current_poster
import flux.shared.generated.resources.gallery_image_download
import flux.shared.generated.resources.gallery_use_as_backdrop
import flux.shared.generated.resources.gallery_use_as_poster
import flux.shared.generated.resources.ic_download
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val DISABLED_CONTAINER_COLOR = Color.White.copy(alpha = .12f)
private val DISABLED_CONTENT_COLOR = Color.White.copy(alpha = .6f)

/**
 * The viewer controls, centred at the bottom of the screen above the navigation bar, fading in and out with
 * [visible]: Use as Poster/Backdrop and Image Download in a split button, or Image Download alone when Use as
 * Poster/Backdrop is hidden.
 */
@Composable
fun BoxScope.ViewerControls(
    visible: Boolean,
    imageType: GalleryImageType?,
    artworkImageButton: ArtworkImageButton,
    imageDownloadRunning: Boolean,
    onUseImage: () -> Unit,
    onDownload: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && imageType != null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        // Keeps the content empty if the image vanishes mid fade-out
        if (imageType == null) return@AnimatedVisibility
        if (artworkImageButton == ArtworkImageButton.HIDDEN) {
            ImageDownloadButton(imageDownloadRunning = imageDownloadRunning, onClick = onDownload)
        } else {
            UseImageSplitButton(
                imageType = imageType,
                artworkImageButton = artworkImageButton,
                imageDownloadRunning = imageDownloadRunning,
                onUseImage = onUseImage,
                onDownload = onDownload,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UseImageSplitButton(
    imageType: GalleryImageType,
    artworkImageButton: ArtworkImageButton,
    imageDownloadRunning: Boolean,
    onUseImage: () -> Unit,
    onDownload: () -> Unit,
) {
    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.TonalLeadingButton(
                colors = viewerButtonColors(),
                enabled = artworkImageButton == ArtworkImageButton.AVAILABLE,
                onClick = onUseImage,
            ) {
                Text(text = stringResource(useImageLabel(imageType, artworkImageButton)))
            }
        },
        trailingButton = {
            val downloadDescription = stringResource(Res.string.gallery_image_download)
            SplitButtonDefaults.TrailingButton(
                modifier = Modifier.semantics { contentDescription = downloadDescription },
                enabled = !imageDownloadRunning,
                colors = viewerButtonColors(),
                elevation = ButtonDefaults.filledTonalButtonElevation(),
                onClick = onDownload,
            ) {
                ImageDownloadIcon(imageDownloadRunning = imageDownloadRunning)
            }
        },
    )
}

/** Tonal colours whose disabled state stays readable on the black viewer background, whatever the theme. */
@Composable
private fun viewerButtonColors() = ButtonDefaults.filledTonalButtonColors(
    disabledContainerColor = DISABLED_CONTAINER_COLOR,
    disabledContentColor = DISABLED_CONTENT_COLOR,
)

private fun useImageLabel(type: GalleryImageType, state: ArtworkImageButton): StringResource {
    val current = state == ArtworkImageButton.CURRENT
    return when (type) {
        GalleryImageType.POSTER -> if (current) Res.string.gallery_current_poster else Res.string.gallery_use_as_poster
        GalleryImageType.BACKDROP -> if (current) Res.string.gallery_current_backdrop else Res.string.gallery_use_as_backdrop
        // The button stays hidden for a Logo until it can become the custom Logo
        GalleryImageType.LOGO -> if (current) Res.string.gallery_current_backdrop else Res.string.gallery_use_as_backdrop
    }
}

/** Standalone Image Download button, shown in place of the split button when Use as Poster/Backdrop is hidden. */
@Composable
private fun ImageDownloadButton(imageDownloadRunning: Boolean, onClick: () -> Unit) {
    val downloadDescription = stringResource(Res.string.gallery_image_download)
    FilledTonalIconButton(
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            disabledContainerColor = DISABLED_CONTAINER_COLOR,
            disabledContentColor = DISABLED_CONTENT_COLOR,
        ),
        modifier = Modifier.semantics { contentDescription = downloadDescription },
        enabled = !imageDownloadRunning,
        onClick = onClick,
    ) {
        ImageDownloadIcon(imageDownloadRunning = imageDownloadRunning)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImageDownloadIcon(imageDownloadRunning: Boolean) {
    if (imageDownloadRunning) {
        LoadingIndicator(modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
    } else {
        Icon(
            modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize),
            painter = painterResource(Res.drawable.ic_download),
            contentDescription = null,
        )
    }
}
