package com.mskd.flux.ui.component.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.component.global.ProgressStatusBar
import com.mskd.flux.ui.component.global.ProgressStatusChip
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.grayScale

@Composable
fun MediaThumbnail(
    modifier: Modifier,
    media: Media,
    hd: Boolean = false
) {

    val contentDescription = when (media) {
        is Episode -> "Season ${media.season} episode ${media.number}, ${media.title}"
        is Movie -> media.title
    }

    Box(
        modifier = modifier.aspectRatio(Ui.Images.ratio_16_9),
        contentAlignment = Alignment.BottomCenter,
        content = {

            FluxImage(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (media.status == Status.WATCHED) it.grayScale() else it },
                media = media,
                hd = hd,
                contentDescription = contentDescription
            )

            ProgressStatusBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                isVisible = media.status == Status.IS_WATCHING,
                progress = { media.progressPercent },
            )

            ProgressStatusChip(
                modifier = Modifier.align(Alignment.Center),
                isWatched = media.status == Status.WATCHED
            )

        }
    )

}