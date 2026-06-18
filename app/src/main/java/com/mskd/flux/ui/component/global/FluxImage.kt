package com.mskd.flux.ui.component.global

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import coil3.video.videoFramePercent
import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge

@Composable
fun FluxImage(
    modifier: Modifier = Modifier,
    path: String,
    hd: Boolean = false,
    contentDescription: String,
    contentScale: ContentScale = ContentScale.Crop,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null
) {
    val context = LocalContext.current
    val urlLow = path.tmdbImage
    val urlHigh = path.tmdbImageLarge

    val hdPainter = rememberAsyncImagePainter(
        model = if (hd) urlHigh else null,
    )

    val hdState by hdPainter.state.collectAsState()

    Box(modifier = modifier) {

        AsyncImage(
            modifier = Modifier.matchParentSize(),
            model = ImageRequest.Builder(context)
                .data(urlLow)
                .crossfade(true)
                .allowHardware(false)
                .build(),
            placeholder = Image.placeholder,
            error = Image.error,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onSuccess = onSuccess
        )

        AnimatedVisibility(
            modifier = Modifier.matchParentSize(),
            visible = hdState is AsyncImagePainter.State.Success,
            enter = fadeIn()
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = hdPainter,
                contentDescription = contentDescription,
                contentScale = contentScale
            )
        }

    }

}

@Composable
fun FluxImage(
    modifier: Modifier,
    media: Media,
    hd: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String
) {

    if (media is Episode && media.imagePath.isNotBlank()) {
        FluxImage(
            modifier = modifier,
            path = media.imagePath,
            hd = hd,
            contentScale = contentScale,
            contentDescription = contentDescription
        )
    } else {
        AsyncImage(
            modifier = modifier,
            model = ImageRequest.Builder(LocalContext.current)
                .data(media.file.path)
                .apply {
                    if (media.status == Status.IS_WATCHING)
                        videoFrameMillis(media.currentTime)
                    else
                        videoFramePercent(.05)
                }
                .crossfade(true)
                .build(),
            contentScale = contentScale,
            placeholder = Image.placeholder,
            error = Image.error,
            contentDescription = contentDescription
        )
    }
}

object Image {

    val placeholder: ColorPainter @Composable get() = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f))

    val error: ColorPainter @Composable get() = ColorPainter(MaterialTheme.colorScheme.error.copy(alpha = .4f))

}