package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedVisibilityScope.PlayerControlButtons(
    modifier: Modifier,
    isPlaying: Boolean,
    sendIntent: (PlayerIntent) -> Unit
) {

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
            Icon(
                painter = painterResource(R.drawable.fast_rewind),
                contentDescription = "backward button"
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
            Icon(
                painter = painterResource(R.drawable.fast_forward),
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
                sendIntent = {}
            )
        }
    }
}