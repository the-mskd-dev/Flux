package com.mskd.flux.screens.player.composables.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.SettingsSheet
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.reset
import flux.shared.generated.resources.subtitle_delay
import org.jetbrains.compose.resources.stringResource

/** Shifts the subtitles of the current Media in steps; the delay is never kept past this Media. */
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

            Column(
                modifier = Modifier.padding(FluxUI.Space.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                Text.Content.Title(text = stringResource(Res.string.subtitle_delay))

                Text.List.Title(text = delayMs.formatDelay())

                Row(horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

                    TextButton(onClick = { sendIntent(PlayerIntent.ChangeSubtitleDelay(deltaMs = -step)) }) {
                        Text.List.Title(text = (-step).formatDelay())
                    }

                    TextButton(onClick = { sendIntent(PlayerIntent.ResetSubtitleDelay) }) {
                        Text.List.Title(text = stringResource(Res.string.reset))
                    }

                    TextButton(onClick = { sendIntent(PlayerIntent.ChangeSubtitleDelay(deltaMs = step)) }) {
                        Text.List.Title(text = step.formatDelay())
                    }

                }

            }

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
