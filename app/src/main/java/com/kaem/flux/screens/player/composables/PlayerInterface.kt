package com.kaem.flux.screens.player.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.media3.exoplayer.ExoPlayer
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui

@Composable
fun PlayerInterface(
    media: Media,
    state: PlayerUiState,
    exoPlayer: ExoPlayer,
    sendIntent: (PlayerIntent) -> Unit
) {

    AnimatedVisibility(
        visible = state.showInterface,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    ) {

        ConstraintLayout(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = .5f))
                .fillMaxSize()
                .statusBarsPadding(),
            constraintSet = PlayerButtonsConstraintSet
        ) {

            PlayerTopBar(
                layoutId = "topBar",
                media = media,
                onBackTap = { sendIntent(PlayerIntent.OnBackTap(exoPlayer.currentPosition)) }
            )

            PlayerControlButtons(
                layoutId = "controlButtons",
                isPlaying = state.isPlaying,
                sendIntent = sendIntent
            )

        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(
    layoutId: String,
    media: Media,
    onBackTap: () -> Unit
) {

    TopAppBar(
        modifier = Modifier
            .layoutId(layoutId)
            .fillMaxWidth(),
        title = { Text.Headline.Medium(text = media.title) },
        subtitle = {

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

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        navigationIcon = {
            BackButton(onTap = onBackTap)
        },
    )

}

@kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerControlButtons(
    layoutId: String,
    isPlaying: Boolean,
    sendIntent: (PlayerIntent) -> Unit
) {

    Row(
        modifier = Modifier.layoutId(layoutId),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FloatingActionButton(
            onClick = { sendIntent(PlayerIntent.OnFastRewind) },
            shape = FloatingActionButtonDefaults.largeShape
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
            onClick = { sendIntent(PlayerIntent.OnFastForward) },
            shape = FloatingActionButtonDefaults.largeShape
        ) {
            Icon(
                painter = painterResource(R.drawable.fast_forward),
                contentDescription = "forward button"
            )
        }

    }

}

val PlayerButtonsConstraintSet = ConstraintSet {

    val topBar = createRefFor("topBar")
    constrain(topBar) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
    }

    val controlButtons = createRefFor("controlButtons")
    constrain(controlButtons) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }

}

@Preview
@Composable
fun PlayerControlButtons_Preview() {

    AppTheme {
        PlayerControlButtons(
            layoutId = "",
            isPlaying = true,
            sendIntent = {}
        )
    }
}