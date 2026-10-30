package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.features.images.domain.LOGO_REQUEST_SIZE
import com.mskd.flux.utils.extensions.tmdbLogo

/**
 * Title of an [Artwork]: its Logo when it has one, its title text otherwise.
 * Nothing is shown while the Logo is loading, the title text is shown when it fails to load.
 */
@Composable
fun ArtworkTitle(
    artwork: Artwork,
    modifier: Modifier = Modifier,
) {

    val logoPath = artwork.logoPath

    if (logoPath.isNullOrEmpty()) {
        ArtworkTitleText(modifier = modifier, title = artwork.title)
        return
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(logoPath.tmdbLogo)
            // The Logo is not drawn until loaded, so its size cannot come from the draw scope
            .size(LOGO_REQUEST_SIZE)
            .build()
    )

    val state by painter.state.collectAsState()

    // No transition: a cached Logo is resolved right after the first composition, so it must not replace a visible title text
    if (state is AsyncImagePainter.State.Error) {
        ArtworkTitleText(modifier = modifier, title = artwork.title)
    } else {
        Image(
            modifier = modifier.fillMaxWidth(),
            painter = painter,
            contentDescription = artwork.title,
            contentScale = ContentScale.Fit
        )
    }

}

@Composable
private fun ArtworkTitleText(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text.MainTitle(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ArtworkTitlePreview(
    artwork: Artwork,
    previewHandler: AsyncImagePreviewHandler,
) {
    FluxThemePreview {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FluxUI.Space.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ArtworkTitle(artwork = artwork)
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@PortraitPreview
@Composable
fun ArtworkTitleWithLogo_Preview() {
    ArtworkTitlePreview(
        artwork = MediaMockups.movieArtwork.copy(logoPath = "/logo.png"),
        previewHandler = AsyncImagePreviewHandler { ColorImage(Color.LightGray.toArgb(), width = 500, height = 180) }
    )
}

@OptIn(ExperimentalCoilApi::class)
@PortraitPreview
@Composable
fun ArtworkTitleWithoutLogo_Preview() {
    ArtworkTitlePreview(
        artwork = MediaMockups.movieArtwork.copy(logoPath = ""),
        // Never requested: an Artwork without Logo loads no image
        previewHandler = AsyncImagePreviewHandler { ColorImage(Color.LightGray.toArgb()) }
    )
}

@OptIn(ExperimentalCoilApi::class)
@PortraitPreview
@Composable
fun ArtworkTitleLogoFailed_Preview() {
    ArtworkTitlePreview(
        artwork = MediaMockups.movieArtwork.copy(logoPath = "/logo.png"),
        previewHandler = AsyncImagePreviewHandler { _, request ->
            AsyncImagePainter.State.Error(painter = null, result = ErrorResult(image = null, request = request, throwable = Exception()))
        }
    )
}
