package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.ProgressText
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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

        ProgressText(
            onFinish = { sendIntent(PlayerIntent.PlayNextEpisode(episode)) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    tint = MaterialTheme.colorScheme.onTertiary,
                    contentDescription = "next episode button"
                )
            },
            text = {

                Text.Title.Large(
                    text = stringResource(R.string.next_episode).uppercase(),
                    color = MaterialTheme.colorScheme.onTertiary
                )

            }
        )

    }

}