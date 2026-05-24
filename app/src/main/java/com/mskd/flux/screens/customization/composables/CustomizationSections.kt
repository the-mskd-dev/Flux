package com.mskd.flux.screens.customization.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.screens.customization.CustomizationIntent
import com.mskd.flux.screens.customization.CustomizationUiState
import com.mskd.flux.screens.settings.SettingsIntent
import com.mskd.flux.screens.settings.SettingsUiState
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
            text = stringResource(R.string.app_theme),
            value = stringResource(state.uiTheme.stringResourceId),
            painter = painterResource(R.drawable.ic_theme),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(CustomizationIntent.ShowThemeDialog) }
        )

    }

}