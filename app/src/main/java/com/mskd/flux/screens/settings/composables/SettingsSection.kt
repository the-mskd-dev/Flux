package com.mskd.flux.screens.settings.composables

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.screens.settings.SettingsIntent
import com.mskd.flux.screens.settings.SettingsUiState
import com.mskd.flux.ui.component.FluxDivider
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.WebLink

@Composable
fun SettingsSection(
    iconColor: Color,
    iconBackgroundColor: Color,
    content: @Composable (Color, Color) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.LARGE)
            .clip(Ui.Shape.Corner.Medium),
        horizontalAlignment = Alignment.Start
    ) { content(iconColor, iconBackgroundColor) }
}

@Composable
fun SettingsCustomisationSection(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
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
            onTap = { sendIntent(SettingsIntent.ShowThemeDialog) }
        )

        FluxDivider()

        SettingsSwitch(
            text = stringResource(R.string.auto_keyboard),
            subText = stringResource(R.string.auto_keyboard_desc),
            checked = state.autoKeyboard,
            onCheckedChange = { sendIntent(SettingsIntent.OnAutoKeyboardCheck(it)) },
            painter = painterResource(R.drawable.ic_keyboard),
            iconColor = iconColor,
            backgroundColor = bgColor,
        )

        FluxDivider()

        val displayedLanguage = state.languageValue?.displayLanguage ?: stringResource(R.string.system)
        SettingsItem(
            text = stringResource(R.string.information_language),
            value = displayedLanguage,
            painter = painterResource(R.drawable.ic_language),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowLanguageDialog) }
        )

    }

}

@Composable
fun SettingsPlayerSection(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = .3f)
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(R.string.button_rewind),
            value = "${state.rewindValue}sec",
            painter = painterResource(R.drawable.fast_rewind),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowRewindDialog) }
        )

        FluxDivider()

        SettingsItem(
            text = stringResource(R.string.button_forward),
            value = "${state.forwardValue}sec",
            painter = painterResource(R.drawable.fast_forward),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowForwardDialog) }
        )

        FluxDivider()

        SettingsSwitch(
            text = stringResource(R.string.external_player),
            subText = stringResource(R.string.watch_on_external_player),
            checked = state.useExternalPlayer,
            painter = painterResource(R.drawable.ic_player),
            iconColor = iconColor,
            backgroundColor = bgColor,
            onCheckedChange = { sendIntent(SettingsIntent.OnExternalPlayerCheck(it)) }
        )

    }

}

@Composable
fun SettingsTmdbSection(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(R.string.tmdb_api_token),
            value = "",
            painter = painterResource(R.drawable.ic_api),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnTokenTap) }
        )

        FluxDivider()

        SettingsItem(
            text = stringResource(R.string.how_to_name_files),
            value = "",
            painter = painterResource(R.drawable.ic_help),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnHowToTap) }
        )

    }

}

@Composable
fun SettingsOtherSection(
    context: Context,
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(R.string.about),
            value = stringResource(R.string.about_desc),
            painter = painterResource(R.drawable.ic_info),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnAboutTap) }
        )

        FluxDivider()

        SettingsItem(
            text = stringResource(R.string.make_a_donation),
            value = stringResource(R.string.support_me_desc),
            painter = painterResource(R.drawable.ic_money),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                WebLink.openPage(
                    context = context,
                    url = Constants.CONTACT.BUY_COFFEE
                )
            }
        )

    }

}

@Composable
fun SettingsSyncSection(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(.7f)
    ) { iconColor, bgColor ->

        val textColor by animateColorAsState(if (state.fullSyncInProgress) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = .8f))
        SettingsItem(
            text = stringResource(R.string.sync_library),
            value = stringResource(if (state.fullSyncInProgress) R.string.sync_in_progress else R.string.sync_library_desc),
            valueColor = textColor,
            painter = painterResource(R.drawable.ic_sync),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowFullSyncDialog(true)) }
        )

    }

}

@Composable
fun SettingsAppInfoSection(
    context: Context,
    appVersion: String?
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .6f)
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(R.string.x),
            value = stringResource(R.string.stay_informed),
            painter = painterResource(R.drawable.ic_social_media),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                WebLink.openPage(
                    context = context,
                    url = Constants.CONTACT.X
                )
            }
        )

        FluxDivider()

        SettingsItem(
            text = stringResource(R.string.sources),
            value = "",
            painter = painterResource(R.drawable.ic_sources),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                WebLink.openPage(
                    context = context,
                    url = Constants.CONTACT.GITHUB
                )
            }
        )

        appVersion?.let {

            FluxDivider()

            SettingsItem(
                text = stringResource(R.string.app_version),
                value = it,
                painter = painterResource(R.drawable.ic_version),
                iconColor = iconColor,
                iconBackgroundColor = bgColor,
                onTap = {
                    WebLink.openPage(
                        context = context,
                        url = Constants.CONTACT.RELEASES
                    )
                }
            )

        }

    }

}