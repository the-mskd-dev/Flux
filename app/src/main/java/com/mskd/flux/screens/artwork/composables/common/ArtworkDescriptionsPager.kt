package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.ui.component.EpisodesDetails
import com.mskd.flux.ui.component.MediaDescriptionDetails
import com.mskd.flux.ui.component.OverviewItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription

@Composable
fun ArtworkDescriptionsPager(
    fullArtwork: FullArtwork,
    currentMedia: Media
) {

    val pageCount = when (fullArtwork) {
        is FullArtwork.FullMovie -> 1
        is FullArtwork.FullShow -> if (fullArtwork.isWatching) 2 else 1
    }

    var currentPage by remember { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = currentPage,
        transitionSpec = {
            if (targetState > initialState) {
                fadeIn() togetherWith fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        }
    ) { i ->

        Card(
            modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM),
            shape = MaterialTheme.shapes.large,
            onClick = {
                when {
                    currentPage < pageCount - 1 -> currentPage++
                    currentPage > 0 -> currentPage--
                }
            }
        ) {

            when (fullArtwork) {

                is FullArtwork.FullMovie -> {

                    OverviewItem(
                        title = stringResource(R.string.summary),
                        description = currentMedia.description,
                        bottomDetails = { MediaDescriptionDetails(currentMedia) }
                    )

                }

                is FullArtwork.FullShow -> {

                    val episode = currentMedia as Episode

                    if (i > 0 || pageCount == 1) {

                        OverviewItem(
                            title = stringResource(R.string.summary),
                            description = fullArtwork.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                        )

                    } else {

                        OverviewItem(
                            title = episode.title,
                            description = currentMedia.description,
                            topDetails = { EpisodesDetails(episode = episode) },
                            bottomDetails = { MediaDescriptionDetails(media = episode) }
                        )

                    }

                }
            }

        }

    }


}



@FluxPreview
@Composable
fun ArtworkDescriptionsPager_Movie_Preview() {
    AppTheme {
        ArtworkDescriptionsPager(
            fullArtwork = MediaMockups.fullMovie,
            currentMedia = MediaMockups.fullMovie.movie
        )
    }
}

@FluxPreview
@Composable
fun ArtworkDescriptionsPager_Show_Preview() {
    AppTheme {
        ArtworkDescriptionsPager(
            fullArtwork = MediaMockups.fullShow.copy(episodes = MediaMockups.episodesWithStatus),
            currentMedia = MediaMockups.episode1.copy(status = Status.IS_WATCHING)
        )
    }
}