package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.tmdbImage

@Composable
fun PlayerNextEpisode(
    modifier: Modifier,
    episode: Episode?,
    sendIntent: (PlayerIntent) -> Unit
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = episode != null,
        enter = fadeIn() + slideInHorizontally { it / 2 },
        exit = fadeOut() + slideOutHorizontally { it / 2 }
    ) {

        episode ?: return@AnimatedVisibility

        Surface(
            color = MaterialTheme.colorScheme.scrim,
            contentColor = Color.White,
            shape = Ui.Shape.Corner.ExtraSmall,
            onClick = { sendIntent(PlayerIntent.PlayNextEpisode(episode = episode)) }
        ) {

            Column(
                modifier = Modifier.padding(Ui.Space.EXTRA_SMALL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
            ) {

                Image(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(16f / 9f),
                    url = episode.imagePath.tmdbImage,
                    contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
                )

                Text.Label.Small("Next episode")

            }

        }

    }

}

@Preview
@Composable
fun PlayerNextEpisode_Preview() {
    AppTheme {
        PlayerNextEpisode(
            modifier = Modifier,
            episode = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}