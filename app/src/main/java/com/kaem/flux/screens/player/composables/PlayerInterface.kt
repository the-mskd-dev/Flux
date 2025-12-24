package com.kaem.flux.screens.player.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.Player
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.theme.Ui

@Composable
fun PlayerInterface(
    media: Media,
    showInterface: Boolean,
    isPlaying: Boolean,
    player: Player,
    sendIntent: (PlayerIntent) -> Unit
) {

    AnimatedVisibility(
        visible = showInterface,
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
                isPlaying = isPlaying,
                sendIntent = sendIntent
            )

            PlayerSeekBar(
                layoutId = "seekBar",
                player = player,
                showInterface = showInterface,
                isPlaying = isPlaying,
                sendIntent = sendIntent
            )

        }

    }

}

val PlayerInterfaceConstraintSet = ConstraintSet {

    val (topBar, controlButtons, seekBar, settings) = createRefsFor("topBar", "controlButtons", "seekBar", "settings")

    constrain(topBar) {
        top.linkTo(parent.top, Ui.Space.MEDIUM)
        start.linkTo(parent.start)
        end.linkTo(settings.start, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints
    }

    constrain(settings) {
        top.linkTo(parent.top)
        end.linkTo(parent.end)
    }

    constrain(controlButtons) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }

    constrain(seekBar) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, Ui.Space.MEDIUM)
    }

}