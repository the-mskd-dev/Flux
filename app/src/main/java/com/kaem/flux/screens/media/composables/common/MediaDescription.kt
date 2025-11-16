package com.kaem.flux.screens.media.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.formattedText
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription
import java.util.Locale


@Composable
fun MediaDescription(media: Media?) {

    media ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {

            if (media is Episode) {
                Text.Label.Medium(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.season_and_episode, media.season, media.number).uppercase(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text.Headline.Medium(
                modifier = Modifier.fillMaxWidth(),
                text = if (media is Episode) media.title else stringResource(R.string.summary),
                color = MaterialTheme.colorScheme.onBackground,
                emphasized = true
            )

        }

        Text.Body.Large(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.8f),
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
        modifier = Modifier
            .fillMaxWidth()
            .alpha(.8f),
        horizontalAlignment = Alignment.Start
    ) {

        Text.Body.Small(
            text = media.releaseDate?.let { stringResource(R.string.release_date, it.formattedText) },
            color = MaterialTheme.colorScheme.onBackground
        )

        Text.Body.Small(
            text = stringResource(R.string.duration, media.duration.minToMs.timeDescription()) ,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (media.voteAverage > 0f) {
            val rate = String.format(Locale.getDefault(),"%.2f", media.voteAverage)
            Text.Body.Small(
                text = stringResource(R.string.rate, rate),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

    }
}

@Preview
@Composable
fun MediaDescription_Movie_Preview() {
    FluxTheme {
        MediaDescription(media = MediaMockups.movie)
    }

}

@Preview
@Composable
fun MediaDescription_Show_Preview() {
    FluxTheme {
        MediaDescription(media = MediaMockups.episode1)
    }
}