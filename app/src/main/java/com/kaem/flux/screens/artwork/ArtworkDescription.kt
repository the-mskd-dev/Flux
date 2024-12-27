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
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.extensions.timeDescription
import java.text.DateFormat
import kotlin.time.Duration.Companion.minutes

@Composable
fun ArtworkDescription(artwork: Artwork?) {

    artwork ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        if (artwork is Episode) {

            Column(modifier = Modifier.fillMaxWidth()) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(.8f),
                    text = stringResource(id = R.string.season_and_episode, artwork.season, artwork.number),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = FluxFontSize.SMALL,
                    fontWeight = FluxWeight.LIGHT,
                    fontStyle = FontStyle.Italic
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = FluxFontSize.LARGE,
                    fontWeight = FluxWeight.MEDIUM
                )

            }

        } else {

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.summary),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.LARGE,
                fontWeight = FluxWeight.MEDIUM
            )

        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.8f),
            text = artwork.description,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = FluxFontSize.MEDIUM,
            textAlign = TextAlign.Justify,
            lineHeight = FluxFontSize.MEDIUM.times(1.1f)
        )

        ArtworkDescriptionDetails(artwork)

    }

}

@Composable
fun ArtworkDescriptionDetails(artwork: Artwork) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        artwork.releaseDate?.let {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = stringResource(R.string.release_date, DateFormat.getDateInstance().format(it)) ,
                fontSize = FluxFontSize.SMALL,
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = FontStyle.Italic
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.8f),
            text = stringResource(R.string.duration, artwork.duration.minutes.inWholeMilliseconds.timeDescription) ,
            fontSize = FluxFontSize.SMALL,
            color = MaterialTheme.colorScheme.onBackground,
            fontStyle = FontStyle.Italic
        )

    }
}