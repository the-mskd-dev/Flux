package com.mskd.flux.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.utils.extensions.grayScale

@Composable
fun MediaThumbnail(
    modifier: Modifier,
    media: Media
) {

    val contentDescription = when (media) {
        is Episode -> "Season ${media.season} episode ${media.number}, ${media.title}"
        is Movie -> media.title
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.BottomCenter,
        content = {

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (media.status == Status.WATCHED) it.grayScale() else it },
                media = media,
                contentDescription = contentDescription
            )

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = media.status == Status.IS_WATCHING
            ) {
                ProgressBar(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth(),
                    media = media
                )
            }

            ProgressStatusChip(
                modifier = Modifier.align(Alignment.Center),
                isWatched = media.status == Status.WATCHED
            )

        }
    )

}