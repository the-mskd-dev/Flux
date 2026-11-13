package com.mskd.flux.screens.player.composables.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.playback_speed
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerPlaybackSpeedSheet(
    selectedSpeed: Float,
    sendIntent: (PlayerIntent) -> Unit
) {

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

            LazyColumn(modifier = Modifier.fillMaxWidth()) {

                stickyHeader {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {

                        Text.Content.Title(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = FluxUI.Space.large, start = FluxUI.Space.medium, end = FluxUI.Space.medium, bottom = FluxUI.Space.medium),
                            text = stringResource(Res.string.playback_speed),
                        )

                        HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    }

                }

                itemsIndexed(Constants.PLAYER.PLAYBACK_SPEEDS) { index, speed ->

                    if (index != 0)
                        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = FluxUI.Space.medium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sendIntent(PlayerIntent.SetPlaybackSpeed(speed = speed))
                                sendIntent(PlayerIntent.ShowSettings(sheet = null))
                            }
                            .padding(horizontal = FluxUI.Space.medium, vertical = FluxUI.Space.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                    ) {

                        Text.List.Title(
                            modifier = Modifier.weight(1f),
                            text = speed.formatSpeed(),
                        )

                        if (speed == selectedSpeed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "current speed"
                            )
                        }

                    }

                }
            }

        }

    }

}

/** "1x", "0.25x", "1.5x". */
fun Float.formatSpeed(): String = toString().removeSuffix(".0") + "x"

@FluxPreview
@Composable
fun PlayerPlaybackSpeedSheet_Preview() {
    FluxTheme {
        PlayerPlaybackSpeedSheet(selectedSpeed = 1f, sendIntent = {})
    }
}
