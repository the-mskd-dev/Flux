package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.SettingsSheet
import com.mskd.flux.ui.components.buttons.SmallButton
import com.mskd.flux.ui.Design

@Composable
fun PlayerSettingsButton(
    modifier: Modifier,
    enabled: Boolean,
    sendIntent: (PlayerIntent) -> Unit
) {

    SmallButton(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top + WindowInsetsSides.End))
            .padding(horizontal = Design.Space.medium),
        onClick = { sendIntent(PlayerIntent.ShowSettings(sheet = SettingsSheet.Settings)) },
        enabled = enabled,
        imageVector = Icons.Default.Settings,
        contentDescription = "Player settings"
    )

}