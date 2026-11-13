package com.mskd.flux.screens.player.composables.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.SettingsSheet
import com.mskd.flux.features.player.ui.PlayerSheetTopBar
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.global.expressivePressScale
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_add
import flux.shared.generated.resources.ic_remove
import flux.shared.generated.resources.subtitle_delay
import flux.shared.generated.resources.subtitle_delay_decrease
import flux.shared.generated.resources.subtitle_delay_increase
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Shifts the subtitles of the current Media in steps; the delay is never kept past this Media. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSubtitleDelaySheet(
    delayMs: Long,
    sendIntent: (PlayerIntent) -> Unit
) {

    val step = Constants.PLAYER.SUBTITLE_DELAY_STEP_MS

    BasicAlertDialog(
        modifier = Modifier.clip(AlertDialogDefaults.shape),
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings(sheet = null)) },
    ) {

        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            Column {

                PlayerSheetTopBar(
                    title = stringResource(Res.string.subtitle_delay),
                    onBack = { sendIntent(PlayerIntent.ShowSettings(sheet = SettingsSheet.Settings)) },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FluxUI.Space.large),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium, Alignment.CenterHorizontally)
                ) {

                    StepButton(
                        icon = painterResource(Res.drawable.ic_remove),
                        contentDescription = stringResource(Res.string.subtitle_delay_decrease),
                        onClick = { sendIntent(PlayerIntent.ChangeSubtitleDelay(deltaMs = -step)) }
                    )

                    AnimatedDelay(
                        delayMs = delayMs,
                    )

                    StepButton(
                        icon = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.subtitle_delay_increase),
                        onClick = { sendIntent(PlayerIntent.ChangeSubtitleDelay(deltaMs = step)) }
                    )

                }

            }

        }

    }

}

/**
 * Expressive step button: the shape morphs on press (from the button's own shapes), and the button
 * squishes slightly under the finger, then bounces back on release.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }

    FilledIconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier
            // requiredSize: the Row must not squeeze one button into an oval when the dialog is narrow
            .requiredSize(64.dp)
            .expressivePressScale(interactionSource)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription
        )
    }

}

/** The delay value: it slides in the direction of the change and fades, with a bouncy spring. */
@Composable
private fun AnimatedDelay(
    delayMs: Long,
    modifier: Modifier = Modifier
) {

    AnimatedContent(
        targetState = delayMs,
        modifier = modifier,
        transitionSpec = {
            val increasing = targetState > initialState
            val motion = spring<IntOffset>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            (slideInVertically(animationSpec = motion) { height -> if (increasing) height / 2 else -height / 2 } +
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                (slideOutVertically(animationSpec = motion) { height -> if (increasing) -height / 2 else height / 2 } +
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
        },
        label = "SubtitleDelayValue"
    ) { value ->
        // Sized by its content, never unbounded: an unbounded width would push the buttons out of the dialog
        Box(contentAlignment = Alignment.Center, modifier = Modifier.widthIn(min = 128.dp)) {
            Text.List.Title(text = value.formatDelay())
        }
    }

}

/** "0 ms", "+300 ms", "-1200 ms". */
fun Long.formatDelay(): String = when {
    this > 0L -> "+$this ms"
    else -> "$this ms"
}

@FluxPreview
@Composable
fun PlayerSubtitleDelaySheet_Preview() {
    FluxTheme {
        PlayerSubtitleDelaySheet(delayMs = 300L, sendIntent = {})
    }
}
