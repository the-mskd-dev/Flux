package com.kaem.flux.screens.player.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.util.TableInfo
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.uppercaseFirstLetter

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    sendIntent: (PlayerIntent) -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings) }
    ) {
        // Sheet content
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text.Headline.Small("Sous-titre")
                Text.Label.Medium("Français")
            }


        }
    }

}

@Preview
@Composable
fun PlayerSettingsSheet_Preview() {
    AppTheme() {
        PlayerSettingsSheet {  }
    }
}