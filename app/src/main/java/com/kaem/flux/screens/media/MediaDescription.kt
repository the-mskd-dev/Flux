package com.kaem.flux.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.ui.component.TextBold
import com.kaem.flux.ui.component.TextBodyLarge
import com.kaem.flux.ui.component.TextLabelSmall
import com.kaem.flux.ui.component.TextSmall
import com.kaem.flux.ui.component.TextTitleLarge
import com.kaem.flux.ui.component.TextTitleSmall
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription
import java.text.DateFormat
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

        if (media is Episode) {

            Column(modifier = Modifier.fillMaxWidth()) {

                TextTitleSmall(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.season_and_episode, media.season, media.number).uppercase(),
                    color = MaterialTheme.colorScheme.primary
                )

                TextTitleLarge(
                    modifier = Modifier.fillMaxWidth(),
                    text = media.title,
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

        } else {

            TextTitleLarge(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.summary),
                color = MaterialTheme.colorScheme.onBackground,
            )

        }

        TextBodyLarge(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.8f),
            text = media.description,
            textAlign = TextAlign.Justify
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

        TextLabelSmall(
            text = media.releaseDate?.let { stringResource(R.string.release_date, DateFormat.getDateInstance().format(it)) },
        )

        TextLabelSmall(
            text = stringResource(R.string.duration, media.duration.minToMs.timeDescription()) ,
        )

        if (media.voteAverage > 0f) {
            val rate = String.format(Locale.getDefault(),"%.2f", media.voteAverage)
            TextLabelSmall(
                text = stringResource(R.string.rate, rate),
            )
        }

    }
}

@Preview
@Composable
fun MediaDescription_Movie_Preview() {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        MediaDescription(media = MediaMockups.movie)
    }

}

@Preview
@Composable
fun MediaDescription_Show_Preview() {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        MediaDescription(media = MediaMockups.episode1)
    }
}