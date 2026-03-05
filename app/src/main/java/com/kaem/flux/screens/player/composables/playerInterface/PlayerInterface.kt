package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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

    val density = LocalDensity.current
    var seekBarHeight by remember { mutableStateOf(0.dp) }

    val nextButtonBottomMargin by animateDpAsState(
        targetValue = if (controls.showInterface) {
            seekBarHeight + Ui.Space.MEDIUM
        } else {
            Ui.Space.LARGE
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NextEpisodeButtonPosition"
    )

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
                    modifier = Modifier.layoutId("topBar"),
                    media = media,
                    onBackTap = { sendIntent(PlayerIntent.OnBackTap(player.currentPosition)) }
                )

                PlayerSettingsButton(
                    modifier = Modifier.layoutId("settings"),
                    sendIntent = sendIntent
                )

                PlayerControlButtons(
                    modifier = Modifier
                        .layoutId("controlButtons")
                        .animateEnterExit(
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            exit = scaleOut()
                        ),
                    isPlaying = controls.isPlaying,
                    sendIntent = sendIntent
                )

                PlayerSeekBar(
                    modifier = Modifier
                        .layoutId("seekBar")
                        .onGloballyPositioned { coordinates ->
                            val height = with(density) { coordinates.size.height.toDp() }
                            if (seekBarHeight != height) seekBarHeight = height
                        },
                    player = player,
                    showInterface = controls.showInterface,
                    isPlaying = controls.isPlaying,
                    sendIntent = sendIntent
                )

            }

        }

        PlayerNextEpisode(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Ui.Space.LARGE, bottom = nextButtonBottomMargin),
            nextButton = controls.nextButton,
            sendIntent = sendIntent
        )

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