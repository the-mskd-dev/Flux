package com.kaem.flux.screens.artwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.ui.component.LightText
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.SmallText
import com.kaem.flux.ui.theme.Dimensions
import com.kaem.flux.utils.extensions.timeDescription
import java.text.DateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

@Composable
fun ArtworkDescription(artwork: Artwork?) {

    artwork ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Space.MEDIUM)
    ) {

        if (artwork is Episode) {

            Column(modifier = Modifier.fillMaxWidth()) {

                SmallText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(.8f),
                    text = stringResource(id = R.string.season_and_episode, artwork.season, artwork.number),
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic
                )

                MediumText(
                    modifier = Modifier.fillMaxWidth(),
                    text = artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = Dimensions.FontSize.LARGE
                )

            }

        } else {

            MediumText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.summary),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = Dimensions.FontSize.LARGE
            )

        }

        LightText(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.8f),
            text = artwork.description,
            textAlign = TextAlign.Justify
        )

        ArtworkDescriptionDetails(artwork)

    }

}

@Composable
fun ArtworkDescriptionDetails(artwork: Artwork) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(.8f),
        horizontalAlignment = Alignment.Start
    ) {

        SmallText(
            text = artwork.releaseDate?.let { stringResource(R.string.release_date, DateFormat.getDateInstance().format(it)) },
            fontStyle = FontStyle.Italic
        )

        SmallText(
            text = stringResource(R.string.duration, artwork.duration.minutes.inWholeMilliseconds.timeDescription) ,
            fontStyle = FontStyle.Italic
        )

        if (artwork.voteAverage > 0f) {
            val rate = String.format(Locale.getDefault(),"%.2f", artwork.voteAverage)
            SmallText(
                text = stringResource(R.string.rate, rate),
                fontStyle = FontStyle.Italic
            )
        }

    }
}