package com.mskd.flux.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.grayScale
import com.mskd.flux.utils.extensions.tmdbImage

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
            .clip(Ui.Shape.Corner.Small)
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

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = media.status == Status.WATCHED
            ) {
                Box(
                    modifier = Modifier
                        .clip(Ui.Shape.Corner.Small)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .height(32.dp)
                        .widthIn(min = 40.dp)
                        .padding(horizontal = Ui.Space.SMALL),
                    contentAlignment = Alignment.Center
                ) {
                    Text.Label.Medium(
                        color = MaterialTheme.colorScheme.onTertiary,
                        text = stringResource(R.string.watched)
                    )
                }
            }

        }
    )

}