package com.mskd.flux.screens.customization.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.screens.customization.CustomizationIntent
import com.mskd.flux.screens.customization.CustomizationUiState
import com.mskd.flux.screens.settings.composables.SettingsItem
import com.mskd.flux.screens.settings.composables.SettingsSection
import com.mskd.flux.screens.settings.composables.SettingsSwitch

@Composable
fun CustomizationThemeSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f)
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(R.string.accent_color),
            value = "",
            painter = painterResource(R.drawable.ic_palette),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(CustomizationIntent.ShowColorDialog) }
        )

        SettingsItem(
            text = stringResource(R.string.app_theme),
            value = stringResource(state.uiTheme.stringResourceId),
            painter = painterResource(R.drawable.ic_theme),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(CustomizationIntent.ShowThemeDialog) }
        )

    }

}

@Composable
fun CustomizationPlayerSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = .3f)
    ) { iconColor, bgColor ->

        SettingsSwitch(
            text = stringResource(R.string.wave_progress),
            subText = "",
            checked = state.waveProgress,
            onCheckedChange = { sendIntent(CustomizationIntent.OnWaveProgressCheck(it)) },
            painter = painterResource(R.drawable.ic_wave_progress),
            iconColor = iconColor,
            backgroundColor = bgColor,
        )

    }

}

@Composable
fun ColorItem(argb: Int?) {

    argb ?: return

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .background(Color(argb))
    )

}