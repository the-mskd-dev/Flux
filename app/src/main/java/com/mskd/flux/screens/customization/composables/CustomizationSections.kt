package com.mskd.flux.screens.customization.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.screens.customization.CustomizationIntent
import com.mskd.flux.screens.customization.CustomizationUiState
import com.mskd.flux.screens.settings.composables.SettingsItem
import com.mskd.flux.screens.settings.composables.SettingsSection
import com.mskd.flux.screens.settings.composables.SettingsSwitch
import com.mskd.flux.ui.theme.Ui

@Composable
fun CustomizationThemeSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection { _, _ ->

        SettingsItem(
            text = stringResource(R.string.accent_color),
            subText = stringResource(Ui.AccentColors.findColor(state.color)?.stringResId ?: R.string.accent_color_desc),
            onTap = { sendIntent(CustomizationIntent.ShowColorDialog) }
        )

        SettingsItem(
            text = stringResource(R.string.app_theme),
            subText = stringResource(state.uiTheme.stringResourceId),
            onTap = { sendIntent(CustomizationIntent.ShowThemeDialog) }
        )

    }

}

@Composable
fun CustomizationPlayerSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection { _, _ ->

        SettingsSwitch(
            text = stringResource(R.string.wave_progress),
            checked = state.waveProgress,
            onCheckedChange = { sendIntent(CustomizationIntent.OnWaveProgressCheck(it)) },
        )

    }

}

@Composable
fun ColorItem(color: Color?) {

    color ?: return

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .background(color)
    )

}