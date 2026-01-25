package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.ProgressButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerNextEpisode(
    modifier: Modifier,
    episode: Episode?,
    sendIntent: (PlayerIntent) -> Unit
) {

    AnimatedVisibility(
        modifier = modifier.clickable { episode?.let { sendIntent(PlayerIntent.PlayNextEpisode(it)) } },
        visible = episode != null,
        enter = fadeIn() + slideInHorizontally { it / 2 },
        exit = fadeOut() + slideOutHorizontally { it / 2 }
    ) {

        episode ?: return@AnimatedVisibility

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {
            FloatingActionButton(
                onClick = { sendIntent(PlayerIntent.TogglePlayButton) },
                shape = FloatingActionButtonDefaults.mediumShape
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = "play button"
                )
            }

            ProgressButton(
                onFinish = { sendIntent(PlayerIntent.PlayNextEpisode(episode)) },
                icon = { size ->
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                        painter = painterResource(R.drawable.skip_next),
                        tint = MaterialTheme.colorScheme.onTertiary,
                        contentDescription = "cancel next episode button"
                    )
                },
                text = { size ->
                    Text.Adaptive(
                        text = stringResource(R.string.next_episode).uppercase(),
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = ButtonDefaults.textStyleFor(size)
                    )
                }
            )

        }

    }

}

@Preview
@Composable
fun PlayerNextEpisode_Preview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            PlayerNextEpisode(
                modifier = Modifier,
                episode = MediaMockups.episode1,
                sendIntent = {}
            )
        }
    }
}