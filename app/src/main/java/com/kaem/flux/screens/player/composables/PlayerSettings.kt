package com.kaem.flux.screens.player.composables

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSettings(
    layoutId: String,
    sendIntent: (PlayerIntent) -> Unit
) {

    IconButton(
        modifier = Modifier
            .layoutId(layoutId)
            .statusBarsPadding(),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.White
        ),
        onClick = { sendIntent(PlayerIntent.ShowSettings) }
    ) {

        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Player settings"
        )

    }

}