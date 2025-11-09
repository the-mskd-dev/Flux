package com.kaem.flux.screens.media.composables.episodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun MediaEpisodeImage(
    episode: Episode,
    width: Dp
) {

    Box(
        modifier = Modifier
            .clip(Ui.Shape.Corner.ExtraSmall)
            .width(width)
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.BottomCenter,
        content = {

            Image(
                modifier = Modifier.fillMaxSize(),
                url = Constants.TMDB.IMAGE + episode.imagePath,
                contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
            )

            if (episode.status == Status.IS_WATCHING) {
                ProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    media = episode
                )
            }

        }
    )

}