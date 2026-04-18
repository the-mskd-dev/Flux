package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.BackButton
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.tmdbImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkImage(
    modifier: Modifier,
    artwork: Artwork,
    sendIntent: (ArtworkIntent) -> Unit
) {

    var imageHeight by remember { mutableIntStateOf(0) }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(artwork.imagePath.tmdbImage)
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
            alpha = .2f,
            contentDescription = "background ${artwork.title}"
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
            modifier = Modifier.fillMaxSize(),
        ) {

            TopAppBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                title = {  },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    BackButton(onTap = { sendIntent(ArtworkIntent.OnBackTap) })
                },
            )

            AsyncImage(
                modifier = Modifier
                    .clickable { sendIntent(ArtworkIntent.OpenArtworkInfo(artwork = artwork)) }
                    .align(Alignment.Center)
                    .clip(Ui.Shape.Corner.Small)
                    .width(160.dp)
                    .aspectRatio(2f/3f),
                model = imageRequest,
                contentScale = ContentScale.Crop,
                placeholder = Image.placeholder,
                error = Image.error,
                contentDescription = artwork.title
            )

        }

    }

}

@FluxPreview
@Composable
fun ArtworkImage_Preview() {
    AppTheme {
        ArtworkImage(
            modifier = Modifier.aspectRatio(6f / 5f),
            artwork = MediaMockups.showArtwork,
            sendIntent = {},
        )
    }
}