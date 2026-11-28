package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import com.mskd.flux.ui.Design
import kotlinx.coroutines.flow.StateFlow

private val EmptyShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Rectangle(Rect.Zero)
}

@Composable
fun PlayerInterface(
    modifier: Modifier = Modifier,
    session: PlayerSession<Any>,
    interfaceState: StateFlow<PlayerInterfaceState>,
    overlays: StateFlow<PlayerOverlays>,
    isPlaying: StateFlow<Boolean>,
    progress: () -> Long,
    sendIntent: (PlayerIntent) -> Unit
) {

    val showInterface by interfaceState.collectSelectedAsState { it.showInterface }
    val isPlayingState = isPlaying.collectAsStateWithLifecycle()
    val chapterButton = overlays.collectSelectedAsState { it.chapterButton }
    var seekBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // The Interface stays composed: its appearance is replayed by this transition, read in the draw phase only
    val transition = updateTransition(targetState = showInterface, label = "PlayerInterfaceVisibility")
    val alpha = transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "PlayerInterfaceAlpha"
    ) { if (it) 1f else 0f }
    val buttonsScale = transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            } else {
                spring(stiffness = Spring.StiffnessMediumLow)
            }
        },
        label = "PlayerControlButtonsScale"
    ) { if (it) 1f else 0f }
    val buttonsSlide = transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "PlayerControlButtonsSlide"
    ) { if (it) 1f else 0f }

    // Visible, appearing or fading out (as with AnimatedVisibility): once fully hidden, the wavy track is
    // no longer composed and no element receives click or focus. Disabled styling never shows during the fade.
    val isActive = transition.currentState || transition.targetState

    Box(modifier = modifier) {

        ConstraintLayout(
            modifier = Modifier
                .graphicsLayer {
                    val currentAlpha = alpha.value
                    this.alpha = currentAlpha
                    if (currentAlpha <= 0.001f) {
                        clip = true
                        shape = EmptyShape
                    } else {
                        clip = false
                        shape = RectangleShape
                    }
                }
                .focusProperties { canFocus = isActive }
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = .5f))
                .fillMaxSize(),
            constraintSet = PlayerInterfaceConstraintSet
        ) {

            PlayerTopBar(
                modifier = Modifier.layoutId("topBar"),
                media = session.media,
                enabled = isActive,
                onBackTap = { sendIntent(PlayerIntent.OnBackTap) }
            )

            PlayerSettingsButton(
                modifier = Modifier.layoutId("settings"),
                enabled = isActive,
                sendIntent = sendIntent
            )

            // Never set scaleX/scaleY to 0f: a singular matrix caches Float.NaN in Compose's
            // GraphicsLayerOwnerLayer.inverseMatrixCache, which is not cleared on returning to 1f (identity),
            // breaking hit-testing on first appearance. Clip to an empty outline at alpha 0f instead.
            PlayerControlButtons(
                modifier = Modifier
                    .layoutId("controlButtons")
                    .graphicsLayer {
                        val scale = buttonsScale.value
                        if (scale <= 0.001f) {
                            this.alpha = 0f
                            scaleX = 1f
                            scaleY = 1f
                            clip = true
                            shape = EmptyShape
                        } else {
                            this.alpha = 1f
                            scaleX = scale
                            scaleY = scale
                            clip = false
                            shape = RectangleShape
                        }
                    },
                isPlaying = isPlayingState.value,
                rewindAndForward = session.playerRewind to session.playerForward,
                slide = { buttonsSlide.value },
                sendIntent = sendIntent
            )

            PlayerSeekBar(
                modifier = Modifier
                    .layoutId("seekBar")
                    .onSizeChanged { size ->
                        val height = with(density) { size.height.toDp() }
                        if (seekBarHeight != height) seekBarHeight = height
                    },
                isPlaying = isPlayingState.value,
                progress = progress,
                duration = session.duration,
                enabled = isActive,
                showWave = isActive,
                chapters = session.chapters,
                sendIntent = sendIntent,
            )

        }

        val bottomInset = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues().calculateBottomPadding()

        PlayerChapterButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.End))
                .padding(end = Design.Space.large),
            chapterButton = chapterButton.value,
            bottomMargin = {
                if (showInterface) {
                    // seekBarHeight already includes bottomInset (from PlayerSeekBar's windowInsetsPadding)
                    seekBarHeight + Design.Space.medium
                } else {
                    bottomInset + Design.Space.large
                }
            },
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
        top.linkTo(parent.top, Design.Space.medium)
        start.linkTo(parent.start)
        end.linkTo(settings.start, Design.Space.medium)
        width = Dimension.fillToConstraints
    }

    constrain(controlButtons) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }

    constrain(settings) {
        top.linkTo(topBar.top)
        bottom.linkTo(topBar.bottom)
        end.linkTo(parent.end)
    }

    constrain(seekBar) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, Design.Space.medium)
    }

    constrain(nextEpisode) {
        end.linkTo(parent.end, Design.Space.medium)
        bottom.linkTo(seekBar.top)
    }

}