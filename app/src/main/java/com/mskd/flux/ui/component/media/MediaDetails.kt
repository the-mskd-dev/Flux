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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import com.mskd.flux.utils.extensions.toRating

@Composable
fun MediaDetailsHorizontal(media: Media) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        MediaDetailsItems(media = media)

    }

}

@Composable
fun MediaDetailsVertical(media: Media) {

    Column {

        MediaDetailsItems(media = media)

    }

}

@Composable

fun MediaDetailsItems(media: Media) {

    media.releaseDate?.let {

        MediaDetailItem(
            painter = painterResource(R.drawable.ic_date),
            text = it.formattedText,
            contentDescription = "release date icon"
        )

    }

    MediaDetailItem(
        painter = painterResource(R.drawable.ic_time),
        text = media.duration.minToMs.timeDescription(),
        contentDescription = "duration icon"
    )


    if (media.artworkId != Artwork.UNKNOWN_ID) {

        MediaDetailItem(
            painter = painterResource(R.drawable.ic_rating),
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
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
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
    AppTheme {
        MediaDetailsHorizontal(
            media = MediaMockups.episode1
        )
    }
}

@Preview
@Composable
fun MediaDetailsVertical_Preview() {
    AppTheme {
        MediaDetailsVertical(
            media = MediaMockups.episode1
        )
    }
}