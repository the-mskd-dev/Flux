package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.Ui

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(
    layoutId: String,
    media: Media,
    onBackTap: () -> Unit
) {

    Row(
        modifier = Modifier
            .layoutId(layoutId)
            .statusBarsPadding()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {

        BackButton(
            onTap = onBackTap,
            tint = Color.White
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
        ) {

            Text.Body.Large(
                text = media.title,
                color = Color.White
            )

            (media as? Episode)?.let { episode ->

                val season = stringResource(R.string.season, episode.season)
                val number = stringResource(R.string.episode, episode.number)

                Text.Body.Small(
                    modifier = Modifier.fillMaxWidth(),
                    text = "$season, $number",
                    color = Color.White,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }

    }

}