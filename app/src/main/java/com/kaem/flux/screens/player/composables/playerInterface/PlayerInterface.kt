package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.Player
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.theme.Ui

@Composable
fun PlayerInterface(
    media: Media,
    player: Player,
    controlsState: () -> PlayerUiState.Controls,
    sendIntent: (PlayerIntent) -> Unit
) {

    val controls = controlsState()

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = controls.showInterface,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        ) {

            ConstraintLayout(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .5f))
                    .fillMaxSize(),
                constraintSet = PlayerInterfaceConstraintSet
            ) {

                PlayerTopBar(
                    layoutId = "topBar",
                    media = media,
                    onBackTap = { sendIntent(PlayerIntent.OnBackTap(player.currentPosition)) }
                )

                PlayerSettingsButton(
                    layoutId = "settings",
                    sendIntent = sendIntent
                )

                PlayerControlButtons(
                    layoutId = "controlButtons",
                    isPlaying = controls.isPlaying,
                    sendIntent = sendIntent
                )

                PlayerSeekBar(
                    layoutId = "seekBar",
                    player = player,
                    showInterface = controls.showInterface,
                    isPlaying = controls.isPlaying,
                    sendIntent = sendIntent
                )

            }

        }

        if (!controls.showInterface) {

            PlayerNextEpisode(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Ui.Space.LARGE),
                episode = controls.nextEpisode,
                sendIntent = sendIntent
            )

        }

    }

}

val PlayerInterfaceConstraintSet = ConstraintSet {

    val (topBar, controlButtons, seekBar, settings, nextEpisode) = createRefsFor(
        "topBar",
        "controlButtons",
        "seekBar",
        "settings",
        "nextEpisode"
    )

    constrain(topBar) {
        top.linkTo(parent.top, Ui.Space.MEDIUM)
        start.linkTo(parent.start)
        end.linkTo(settings.start, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints
    }

    constrain(controlButtons) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }

    constrain(settings) {
        top.linkTo(parent.top)
        end.linkTo(parent.end)
    }

    constrain(seekBar) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, Ui.Space.MEDIUM)
    }

    constrain(nextEpisode) {
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        bottom.linkTo(seekBar.top)
    }

}