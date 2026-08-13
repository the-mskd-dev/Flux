package com.mskd.flux.screens.customization.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.customization.presentation.CustomizationIntent
import com.mskd.flux.features.customization.presentation.CustomizationUiState
import com.mskd.flux.screens.settings.composables.SettingsItem
import com.mskd.flux.screens.settings.composables.SettingsSection
import com.mskd.flux.screens.settings.composables.SettingsSwitch
import com.mskd.flux.utils.UiCommon
import com.mskd.flux.utils.extensions.resolve
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.accent_color
import flux.shared.generated.resources.accent_color_desc
import flux.shared.generated.resources.app_theme
import flux.shared.generated.resources.corners
import flux.shared.generated.resources.corners_desc
import flux.shared.generated.resources.items
import flux.shared.generated.resources.items_per_row
import flux.shared.generated.resources.large_episode_image
import flux.shared.generated.resources.navigation_style
import flux.shared.generated.resources.old_blurred_header
import flux.shared.generated.resources.seasons_per_row
import flux.shared.generated.resources.wave_progress
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomizationThemeSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection { _, _ ->

        SettingsItem(
            text = stringResource(Res.string.accent_color),
            subText = stringResource(UiCommon.AccentColors.findColor(state.color)?.stringResId ?: Res.string.accent_color_desc),
            onClick = { sendIntent(CustomizationIntent.ShowColorDialog) }
        )

        SettingsItem(
            text = stringResource(Res.string.app_theme),
            subText = stringResource(state.uiTheme.stringResource),
            onClick = { sendIntent(CustomizationIntent.ShowThemeDialog) }
        )

    }

}

@Composable
fun CustomizationGlobalSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection { _, _ ->

        SettingsItem(
            text = stringResource(Res.string.items_per_row),
            subText = stringResource(Res.string.items, state.itemsPerRow),
            onClick = { sendIntent(CustomizationIntent.ShowItemsPerRowDialog) }
        )

        SettingsItem(
            text = stringResource(Res.string.seasons_per_row),
            subText = stringResource(Res.string.items, state.seasonsPerRow),
            onClick = { sendIntent(CustomizationIntent.ShowSeasonsPerRowDialog) }
        )

        SettingsItem(
            text = stringResource(Res.string.corners),
            subText = stringResource(Res.string.corners_desc),
            onClick = { sendIntent(CustomizationIntent.ShowItemsCornerDialog) }
        )

        SettingsItem(
            text = stringResource(Res.string.navigation_style),
            subText = state.navigationStyle.description.resolve(),
            onClick = { sendIntent(CustomizationIntent.ShowNavigationStyleDialog) }
        )

    }

}

@Composable
fun CustomizationArtworkSection(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    SettingsSection { _, _ ->

        SettingsSwitch(
            text = stringResource(Res.string.old_blurred_header),
            checked = state.oldBlurredHeader,
            onCheckedChange = { sendIntent(CustomizationIntent.OnOldBlurredHeaderCheck(it)) },
        )

        SettingsSwitch(
            text = stringResource(Res.string.large_episode_image),
            checked = state.largeEpisodeImage,
            onCheckedChange = { sendIntent(CustomizationIntent.OnLargeEpisodeImageCheck(it)) },
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
            text = stringResource(Res.string.wave_progress),
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