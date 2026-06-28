package com.mskd.flux.ui.component.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.releaseDate
import com.mskd.flux.utils.extensions.timeDescription
import com.mskd.flux.utils.extensions.toRating
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episode
import flux.shared.generated.resources.ic_date
import flux.shared.generated.resources.ic_rating
import flux.shared.generated.resources.ic_time
import flux.shared.generated.resources.season
import flux.shared.generated.resources.season_and_episode
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MediaDetailsHorizontal(media: Media) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        MediaDetailsItems(media = media)

    }

}

@Composable
fun MediaDetailsVertical(media: Media) {

    Column {

        if (media is Episode && media.isUnknown) {

            val seasonAndEpisode = when {
                (media.season >= 0 && media.number >= 0) -> stringResource(Res.string.season_and_episode, media.season, media.number)
                media.season >= 0 -> stringResource(Res.string.season, media.season)
                media.number >= 0 -> stringResource(Res.string.episode, media.number)
                else -> null
            }

            Text.Adaptive(
                text = seasonAndEpisode?.lowercase(),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
            )

        }

        MediaDetailsItems(media = media)

    }

}

@Composable

fun MediaDetailsItems(media: Media) {

    media.releaseDate?.formattedText?.let {

        MediaDetailItem(
            painter = painterResource(Res.drawable.ic_date),
            text = it,
            contentDescription = "release date icon"
        )

    }

    MediaDetailItem(
        painter = painterResource(Res.drawable.ic_time),
        text = media.duration.minToMs.timeDescription(),
        contentDescription = "duration icon"
    )


    if (media.artworkId != Artwork.UNKNOWN_ID) {

        MediaDetailItem(
            painter = painterResource(Res.drawable.ic_rating),
            text = "${media.voteAverage.toRating}/10",
            contentDescription = "rating icon"
        )

    }

}

@Composable
fun MediaDetailItem(
    painter: Painter,
    text: String,
    contentDescription: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {


        Icon(
            modifier = Modifier.size(12.dp),
            painter = painter,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = contentDescription
        )

        Text.Body.Small(
            text = text,
            color = MaterialTheme.colorScheme.secondary
        )

    }

}

@Preview
@Composable
fun MediaDetailsHorizontal_Preview() {
    FluxTheme {
        MediaDetailsHorizontal(
            media = MediaMockups.episode1
        )
    }
}

@Preview
@Composable
fun MediaDetailsVertical_Preview() {
    FluxTheme {
        MediaDetailsVertical(
            media = MediaMockups.episode1
        )
    }
}

@Preview
@Composable
fun MediaDetailsHorizontal_Unknown_Preview() {
    FluxTheme {
        MediaDetailsHorizontal(
            media = MediaMockups.unknownEpisode
        )
    }
}

@Preview
@Composable
fun MediaDetailsVertical_Unknown_Preview() {
    FluxTheme {
        MediaDetailsVertical(
            media = MediaMockups.unknownEpisode
        )
    }
}