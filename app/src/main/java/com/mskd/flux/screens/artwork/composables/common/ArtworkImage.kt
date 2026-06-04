package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkImage(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media? = null,
    orientation: Orientation = Orientation.Vertical
) {

    val episode = currentMedia as? Episode
    val imagePath = episode?.imagePath ?: fullArtwork.artwork.bannerPath
    var imageSize by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.onSizeChanged { imageSize = if (orientation == Orientation.Vertical) it.height else it.width }) {

        FluxImage(
            modifier = Modifier
                .matchParentSize()
                .alpha(.8f),
            path = imagePath,
            hd = true,
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

@FluxPreview
@Composable
fun ArtworkImage_Preview() {
    AppTheme {
        ArtworkImage(
            modifier = Modifier.aspectRatio(6f / 5f),
            fullArtwork = MediaMockups.fullShow,
            currentMedia = MediaMockups.fullShow.episodes.first(),
        )
    }
}