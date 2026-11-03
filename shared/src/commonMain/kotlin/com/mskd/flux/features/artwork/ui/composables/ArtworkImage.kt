package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.core.model.artwork.ArtworkHeaderMode
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.artworkImagePath
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.blurContent
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.Image
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.tmdbImage

@Composable
fun ArtworkImage(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media? = null,
    selectedSeason: Int? = null,
    orientation: Orientation = Orientation.Vertical,
    isBlurred: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    val clickableModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    if (FluxUI.global.oldBlurredHeader) {
        ArtworkImageBlurred(
            modifier = clickableModifier,
            fullArtwork = fullArtwork,
            selectedSeason = selectedSeason
        )
    } else {
        ArtworkImageFull(
            modifier = clickableModifier,
            fullArtwork = fullArtwork,
            currentMedia = currentMedia,
            orientation = orientation,
            isBlurred = isBlurred
        )
    }

}

@Composable
fun ArtworkImageFull(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media? = null,
    orientation: Orientation = Orientation.Vertical,
    isBlurred: Boolean = false
) {

    val episode = currentMedia as? Episode
    val imagePath = episode?.imagePath ?: fullArtwork.artwork.artworkImagePath(ArtworkHeaderMode.NORMAL)
    var imageSize by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.onSizeChanged { imageSize = if (orientation == Orientation.Vertical) it.height else it.width }) {

        FluxImage(
            modifier = Modifier
                .matchParentSize()
                // Stronger than the Episode thumbnails: the image is much larger, a light blur still shows the scene.
                // Stronger than the Episode thumbnails: the image is much larger, a light blur still shows the scene.
                .blurContent(blurred = isBlurred, radius = 24.dp)
                .alpha(.8f),
            path = imagePath,
            contentDescription = "background ${fullArtwork.artwork.title}"
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (orientation == Orientation.Vertical) {
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = .1f),
                                    MaterialTheme.colorScheme.background.copy(alpha = .3f),
                                    MaterialTheme.colorScheme.background.copy(alpha = .5f),
                                    MaterialTheme.colorScheme.background.copy(alpha = .7f),
                                    MaterialTheme.colorScheme.background.copy(alpha = .9f),
                                    MaterialTheme.colorScheme.background,
                                ),
                                startY = imageSize * .6f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                    } else {
                        Modifier
                    }
                )
        )

    }

}

@Composable
fun ArtworkImageBlurred(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    selectedSeason: Int?,
    ratio: Float = 2f/3f
) {

    val imageUrl = when (fullArtwork) {
        is FullArtwork.FullMovie -> fullArtwork.artwork.artworkImagePath(ArtworkHeaderMode.BLURRED).tmdbImage
        is FullArtwork.FullShow -> fullArtwork.seasons.find { it.season == selectedSeason }?.imagePath?.tmdbImage ?: fullArtwork.imagePath.tmdbImage
    }

    var imageHeight by remember { mutableIntStateOf(0) }
    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(imageUrl)
        .crossfade(true)
        .build()

    Box(modifier = modifier.onSizeChanged { imageHeight = it.height }) {

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .blur(radius = 15.dp),
            model = imageRequest,
            contentScale = ContentScale.Crop,
            placeholder = Image.placeholder,
            error = Image.error,
            alpha = .9f,
            contentDescription = "background ${fullArtwork.artwork.title}"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = .6f),
                            MaterialTheme.colorScheme.background.copy(alpha = .9f),
                            MaterialTheme.colorScheme.background,
                        ),
                        startY = imageHeight * .7f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(.7f),
            contentAlignment = Alignment.Center
        ) {

            AsyncImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(FluxUI.shapes.corners)
                    .aspectRatio(ratio)
                    .fillMaxSize(),
                model = imageRequest,
                contentScale = ContentScale.Crop,
                placeholder = Image.placeholder,
                error = Image.error,
                contentDescription = fullArtwork.artwork.title
            )

        }

    }

}

@FluxPreview
@Composable
fun ArtworkImage_Full_Preview() {
    FluxTheme {
        ArtworkImageFull(
            modifier = Modifier.aspectRatio(6f / 5f),
            fullArtwork = MediaMockups.fullShow,
            currentMedia = MediaMockups.fullShow.episodes.first(),
        )
    }
}

@FluxPreview
@Composable
fun ArtworkImage_Blurred_Preview() {
    FluxTheme {
        ArtworkImageBlurred(
            modifier = Modifier.aspectRatio(6f / 5f),
            fullArtwork = MediaMockups.fullShow,
            selectedSeason = null
        )
    }
}