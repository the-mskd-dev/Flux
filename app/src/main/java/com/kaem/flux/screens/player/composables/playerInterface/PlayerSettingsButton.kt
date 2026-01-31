package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSettingsButton(
    modifier: Modifier,
    sendIntent: (PlayerIntent) -> Unit
) {

    IconButton(
        modifier = modifier.statusBarsPadding(),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.White
        ),
        onClick = { sendIntent(PlayerIntent.ShowSettings(sheet = PlayerUiState.SettingsSheet.Settings)) }
    ) {

        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Player settings"
        )

    }

}