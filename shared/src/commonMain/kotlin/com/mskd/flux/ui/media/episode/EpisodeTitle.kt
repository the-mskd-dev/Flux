package com.mskd.flux.ui.media.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.ui.blurText
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun EpisodeTitle(
    episode: Episode,
    isSelected: Boolean,
    isBlurred: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
    ) {

        if (episode.number >= 0 && !episode.isUnknown) {
            Text.Card.Title(
                text =  "${episode.number}.",
                textAlign = TextAlign.Start,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
            )
        }

        Text.Card.Title(
            modifier = Modifier
                .weight(1f)
                .blurText(blurred = isBlurred),
            text =  episode.title,
            textAlign = TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        )

    }

}