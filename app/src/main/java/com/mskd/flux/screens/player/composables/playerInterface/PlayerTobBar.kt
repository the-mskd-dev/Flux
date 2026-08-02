package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episode
import flux.shared.generated.resources.season
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(
    modifier: Modifier,
    media: Media,
    onBackTap: () -> Unit
) {

    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = modifier
                .statusBarsPadding()
                .clickable { onBackTap() }
                .size(50.dp)
                .clip(shape = CircleShape)
                .padding(FluxUI.Space.extraSmall),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                tint = Color.White,
                contentDescription = "back button"
            )

        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            Text.Content.Body(
                text = media.title,
                color = Color.White
            )

            (media as? Episode)?.let { episode ->

                val season = stringResource(Res.string.season, episode.season)
                val number = stringResource(Res.string.episode, episode.number)

                Text.Card.Label(
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