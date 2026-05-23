package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .padding(horizontal = Ui.Space.MEDIUM)
            .clickable {
                when {
                    currentPage < pageCount - 1 -> currentPage++
                    currentPage > 0 -> currentPage--
                }
            },
        transitionSpec = {
            if (targetState > initialState) {
                fadeIn() togetherWith fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        }
    ) { i ->

        when (fullArtwork) {

            is FullArtwork.FullMovie -> {

                ArtworkDescription(
                    title = stringResource(R.string.summary),
                    description = currentMedia.description,
                    bottomDetails = { MediaDescriptionDetails(currentMedia) }
                )

            }

            is FullArtwork.FullShow -> {

                val episode = currentMedia as Episode


                if (i > 0 || pageCount == 1) {

                    ArtworkDescription(
                        title = stringResource(R.string.summary),
                        description = fullArtwork.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                    )

                } else {

                    ArtworkDescription(
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

@Composable
fun ArtworkDescription(
    title: String,
    description: String,
    topDetails: @Composable () -> Unit = {},
    bottomDetails: @Composable () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(all = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            topDetails()

            Text.Headline.Medium(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                emphasized = true
            )

        }

        Text.Body.Large(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onBackground
        )

        bottomDetails()

    }

}

@Composable
fun EpisodesDetails(episode: Episode) {

    Row(horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {
        Text.Label.Medium(
            text = stringResource(id = R.string.season, episode.season).uppercase(),
            emphasized = true,
            color = MaterialTheme.colorScheme.primary,
        )
        Text.Label.Medium(
            text = stringResource(id = R.string.episode, episode.number).uppercase(),
            emphasized = true,
            color = MaterialTheme.colorScheme.secondary
        )
    }

}

@Composable
fun MediaDescriptionDetails(media: Media) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        Text.Body.Small(
            text = media.releaseDate?.let { stringResource(R.string.release_date, it.formattedText) },
            color = MaterialTheme.colorScheme.secondary
        )

        Text.Body.Small(
            text = stringResource(R.string.duration, media.duration.minToMs.timeDescription()) ,
            color = MaterialTheme.colorScheme.secondary
        )

        if (media.voteAverage > 0f) {
            val rate = String.format(LocalLocale.current.platformLocale,"%.2f", media.voteAverage)
            Text.Body.Small(
                text = stringResource(R.string.rate, rate),
                color = MaterialTheme.colorScheme.secondary
            )
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
            fullArtwork = MediaMockups.fullShow,
            currentMedia = MediaMockups.episode1
        )
    }
}