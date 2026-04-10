package com.mskd.flux.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.utils.extensions.tmdbImage
import kotlin.time.Duration.Companion.seconds

@Composable
fun Image(
    modifier: Modifier,
    url: String,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentScale = contentScale,
        placeholder = Image.placeholder,
        error = Image.error,
        contentDescription = contentDescription
    )
}

@Composable
fun Image(
    modifier: Modifier,
    media: Media,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String
) {

    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .apply {

                if (media is Episode && media.imagePath.isNotBlank()) {
                    data(media.imagePath.tmdbImage)
                } else {
                    data(media.file.path)
                    videoFrameMillis(1.seconds.inWholeMilliseconds)
                }

            }
            .crossfade(true)
            .build(),
        contentScale = contentScale,
        placeholder = Image.placeholder,
        error = Image.error,
        contentDescription = contentDescription
    )
}

object Image {

    val placeholder: ColorPainter @Composable get() = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f))

    val error: ColorPainter @Composable get() = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f))

}