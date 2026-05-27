package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.imageLoader
import coil3.request.ImageRequest
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@Composable
fun ArtworkHeader(
    modifier: Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val isMovie = fullArtwork.artwork.type == ContentType.MOVIE

    ConstraintLayout(
        modifier = modifier
    ) {

        val (image, title, buttons) = createRefs()


        ArtworkImage(
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .aspectRatio(Ui.Images.RATIO_6_5),
            fullArtwork = fullArtwork,
            largeArtworkPoster = true,
            currentMedia = currentMedia,
            sendIntent = sendIntent
        )

        if (isMovie) {
            Text.Display.Small(
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(image.bottom)
                    start.linkTo(parent.start, Ui.Space.MEDIUM)
                    end.linkTo(parent.end, Ui.Space.MEDIUM)
                    bottom.linkTo(image.bottom)
                    width = Dimension.preferredWrapContent
                },
                text = fullArtwork.artwork.title,
                color = MaterialTheme.colorScheme.onBackground,
                emphasized = true
            )
        }

        ArtworkButtons(
            modifier = Modifier.constrainAs(buttons) {
                if (isMovie) top.linkTo(title.bottom, Ui.Space.LARGE)
                else top.linkTo(image.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            media = currentMedia,
            sendIntent = sendIntent
        )

    }

}

@OptIn(ExperimentalCoilApi::class)
@FluxPreview
@Composable
fun ArtworkHeader_Preview() {

    val previewHandler = AsyncImagePreviewHandler { request ->
        request.context.imageLoader.execute(
            ImageRequest.Builder(request.context)
                .data(R.drawable.preview_poster)
                .build()
        ).image!!
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        AppTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {

                ArtworkHeader(
                    modifier = Modifier.fillMaxWidth(),
                    fullArtwork = MediaMockups.fullShow,
                    currentMedia = MediaMockups.episode1,
                    sendIntent = {}
                )

            }
        }
    }

}