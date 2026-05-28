package com.mskd.flux.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription

@Composable
fun OverviewItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    topDetails: @Composable () -> Unit = {},
    bottomDetails: @Composable () -> Unit = {}
) {

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
                color = MaterialTheme.colorScheme.onSurface,
                emphasized = true
            )

        }


        Text.Body.Large(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
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

@Preview
@Composable
fun OverviewItem_Preview_Movie() {
    AppTheme {
        OverviewItem(
            title = stringResource(R.string.summary),
            description = MediaMockups.movie.description,
            bottomDetails = { MediaDescriptionDetails(MediaMockups.movie) }
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Season() {
    AppTheme {
        OverviewItem(
            title = stringResource(R.string.summary),
            description = MediaMockups.season1.description,
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Episode() {
    AppTheme {
        OverviewItem(
            title = MediaMockups.episode1.title,
            description = MediaMockups.episode1.description,
            topDetails = { EpisodesDetails(episode = MediaMockups.episode1) },
            bottomDetails = { MediaDescriptionDetails(media = MediaMockups.episode1) }
        )
    }
}

