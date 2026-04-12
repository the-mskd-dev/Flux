package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import java.util.Locale


@Composable
fun ArtworkDescription(media: Media) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {


            if (media is Episode) {
                Row(horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {
                    Text.Label.Medium(
                        text = stringResource(id = R.string.season, media.season).uppercase(),
                        emphasized = true,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text.Label.Medium(
                        text = stringResource(id = R.string.episode, media.number).uppercase(),
                        emphasized = true,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text.Headline.Medium(
                modifier = Modifier.fillMaxWidth(),
                text = if (media is Episode) media.title else stringResource(R.string.summary),
                color = MaterialTheme.colorScheme.onBackground,
                emphasized = true
            )

        }

        Text.Body.Large(
            modifier = Modifier.fillMaxWidth(),
            text = media.description,
            textAlign = TextAlign.Justify,
            color = MaterialTheme.colorScheme.onBackground
        )

        MediaDescriptionDetails(media)

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
            val rate = String.format(Locale.getDefault(),"%.2f", media.voteAverage)
            Text.Body.Small(
                text = stringResource(R.string.rate, rate),
                color = MaterialTheme.colorScheme.secondary
            )
        }

    }
}

@FluxPreview
@Composable
fun ArtworkDescription_Movie_Preview() {
    AppTheme {
        ArtworkDescription(media = MediaMockups.movie)
    }
}

@FluxPreview
@Composable
fun ArtworkDescription_Show_Preview() {
    AppTheme {
        ArtworkDescription(media = MediaMockups.episode1)
    }
}