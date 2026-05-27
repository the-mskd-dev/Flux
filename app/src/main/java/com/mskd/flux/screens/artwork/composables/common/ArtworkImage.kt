package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkImage(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media,
    largeArtworkPoster: Boolean = false,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val episode = currentMedia as? Episode
    val imageUrl = if (largeArtworkPoster) episode?.imagePath?.tmdbImageLarge ?: fullArtwork.artwork.bannerPath.tmdbImageLarge else fullArtwork.imagePath.tmdbImage
    val placeHolderUrl = if (largeArtworkPoster) episode?.imagePath?.tmdbImage ?: fullArtwork.artwork.bannerPath.tmdbImage else fullArtwork.imagePath.tmdbImage
    val blurIntensity = if (largeArtworkPoster) 0.dp else 15.dp


    var imageHeight by remember { mutableIntStateOf(0) }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .placeholderMemoryCacheKey(placeHolderUrl)
        .build()

    Box(modifier = modifier.onSizeChanged { imageHeight = it.height }) {

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .blur(radius = blurIntensity),
            model = imageRequest,
            contentScale = ContentScale.Crop,
            placeholder = Image.placeholder,
            error = Image.error,
            alpha = 1f,
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
            sendIntent = {},
        )
    }
}