package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.ui.components.buttons.SmallButton
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episode
import flux.shared.generated.resources.season
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerTopBar(
    modifier: Modifier,
    media: Media,
    enabled: Boolean,
    onBackTap: () -> Unit
) {

    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = FluxUI.Space.medium),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SmallButton(
            onClick = onBackTap,
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "back button"
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {

            Text.Content.Title(
                text = media.title,
                color = Color.White
            )

            (media as? Episode)?.let { episode ->

                val season = stringResource(Res.string.season, episode.season)
                val number = stringResource(Res.string.episode, episode.number)

                Text.Card.Label(
                    modifier = Modifier.fillMaxWidth(),
                    text = "$season, $number",
                    color = Color.White.copy(alpha = .7f),
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }

    }

}