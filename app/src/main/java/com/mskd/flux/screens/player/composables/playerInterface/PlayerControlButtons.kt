package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedVisibilityScope.PlayerControlButtons(
    modifier: Modifier,
    isPlaying: Boolean,
    rewindAndForward: () -> Pair<Int, Int>,
    sendIntent: (PlayerIntent) -> Unit
) {

    val (rewind, forward) = rewindAndForward()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FloatingActionButton(
            modifier = Modifier.animateEnterExit(
                enter = slideInHorizontally{ fullWidth -> fullWidth },
                exit = slideOutHorizontally { fullWidth -> fullWidth }
            ),
            onClick = { sendIntent(PlayerIntent.OnFastRewind) },
            shape = FloatingActionButtonDefaults.largeShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (rewind) {
                5 -> R.drawable.ic_rewind_5
                10 -> R.drawable.ic_rewind_10
                else -> R.drawable.ic_rewind_30
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = "rewind button"
            )
        }

        MediumExtendedFloatingActionButton(
            onClick = { sendIntent(PlayerIntent.TogglePlayButton) },
            shape = FloatingActionButtonDefaults.largeExtendedFabShape
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = "play button"
            )
        }

        FloatingActionButton(
            modifier = Modifier.animateEnterExit(
                enter = slideInHorizontally{ fullWidth -> -fullWidth },
                exit = slideOutHorizontally { fullWidth -> -fullWidth }
            ),
            onClick = { sendIntent(PlayerIntent.OnFastForward) },
            shape = FloatingActionButtonDefaults.largeShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (forward) {
                5 -> R.drawable.ic_forward_5
                10 -> R.drawable.ic_forward_10
                else -> R.drawable.ic_forward_30
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = "forward button"
            )
        }

    }

}

@FluxPreview
@Composable
fun PlayerControlButtons_Preview() {

    AppTheme {
        AnimatedContent(targetState = true) {
            PlayerControlButtons(
                modifier = Modifier,
                isPlaying = it,
                rewindAndForward = { 5 to 10 },
                sendIntent = {}
            )
        }
    }
}