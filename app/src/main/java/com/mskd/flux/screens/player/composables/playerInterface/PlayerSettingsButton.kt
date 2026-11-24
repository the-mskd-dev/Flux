package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.SettingsSheet

@Composable
fun PlayerSettingsButton(
    modifier: Modifier,
    enabled: Boolean,
    sendIntent: (PlayerIntent) -> Unit
) {

    IconButton(
        modifier = modifier.statusBarsPadding(),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        onClick = {
            sendIntent(PlayerIntent.ShowSettings(sheet = SettingsSheet.Settings))
        }
    ) {

        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Player settings"
        )

    }

}