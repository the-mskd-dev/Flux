package com.kaem.flux.screens.media.composables.episodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.kaem.flux.R
import com.kaem.flux.model.media.Episode
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.Ui

@Composable
fun MediaEpisodeTitleDesc(
    episode: Episode,
    fixedLines : Boolean
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        Text.Label.Small(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.episode, episode.number).uppercase(),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.primary
        )

        Text.Body.Large(
            modifier = Modifier.fillMaxWidth(),
            text = episode.title,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            emphasized = true
        )

        Text.Body.Medium(
            modifier = Modifier.fillMaxWidth(),
            text = episode.description,
            textAlign = TextAlign.Start,
            minLines = if (fixedLines) 4 else 1,
            maxLines = if (fixedLines) 4 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )

    }
}