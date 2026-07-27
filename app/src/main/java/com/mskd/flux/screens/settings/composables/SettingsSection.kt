package com.mskd.flux.screens.settings.composables

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.features.settings.presentation.SettingsUiState
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.UriUtils
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.about
import flux.shared.generated.resources.about_desc
import flux.shared.generated.resources.app_version
import flux.shared.generated.resources.auto_keyboard
import flux.shared.generated.resources.auto_keyboard_desc
import flux.shared.generated.resources.button_forward
import flux.shared.generated.resources.button_rewind
import flux.shared.generated.resources.cache_images
import flux.shared.generated.resources.cache_images_desc
import flux.shared.generated.resources.caching_images_in_progress
import flux.shared.generated.resources.customization
import flux.shared.generated.resources.customization_desc
import flux.shared.generated.resources.external_player
import flux.shared.generated.resources.fast_forward
import flux.shared.generated.resources.fast_rewind
import flux.shared.generated.resources.how_to_name_files
import flux.shared.generated.resources.ic_api
import flux.shared.generated.resources.ic_customization
import flux.shared.generated.resources.ic_folder
import flux.shared.generated.resources.ic_help
import flux.shared.generated.resources.ic_images
import flux.shared.generated.resources.ic_info
import flux.shared.generated.resources.ic_keyboard
import flux.shared.generated.resources.ic_language
import flux.shared.generated.resources.ic_money
import flux.shared.generated.resources.ic_pip
import flux.shared.generated.resources.ic_player
import flux.shared.generated.resources.ic_social_media
import flux.shared.generated.resources.ic_sources
import flux.shared.generated.resources.ic_sync
import flux.shared.generated.resources.ic_version
import flux.shared.generated.resources.images_cached
import flux.shared.generated.resources.information_language
import flux.shared.generated.resources.make_a_donation
import flux.shared.generated.resources.picture_in_picture
import flux.shared.generated.resources.source_code
import flux.shared.generated.resources.sources
import flux.shared.generated.resources.sources_short_desc
import flux.shared.generated.resources.stay_informed
import flux.shared.generated.resources.support_me_desc
import flux.shared.generated.resources.sync_in_progress
import flux.shared.generated.resources.sync_library
import flux.shared.generated.resources.sync_library_desc
import flux.shared.generated.resources.system
import flux.shared.generated.resources.tmdb_api_token
import flux.shared.generated.resources.watch_on_external_player
import flux.shared.generated.resources.x
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun SettingsSection(
    iconColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f),
    content: @Composable (Color, Color) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FluxUI.Space.large)
            .clip(FluxUI.shapes.corners),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) { content(iconColor, iconBackgroundColor) }
}

@Composable
fun SettingsCustomizationSection(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f)
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(Res.string.customization),
            subText = stringResource(Res.string.customization_desc),
            painter = painterResource(Res.drawable.ic_customization),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnCustomizationTap) }
        )

        SettingsSwitch(
            text = stringResource(Res.string.auto_keyboard),
            subText = stringResource(Res.string.auto_keyboard_desc),
            checked = state.autoKeyboard,
            onCheckedChange = { sendIntent(SettingsIntent.OnAutoKeyboardCheck(it)) },
            painter = painterResource(Res.drawable.ic_keyboard),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
        )

        val displayedLanguage = state.languageValue?.displayLanguage ?: stringResource(Res.string.system)
        SettingsItem(
            text = stringResource(Res.string.information_language),
            subText = displayedLanguage,
            painter = painterResource(Res.drawable.ic_language),
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
            text = stringResource(Res.string.button_rewind),
            subText = "${state.rewindValue}sec",
            painter = painterResource(Res.drawable.fast_rewind),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowRewindDialog) }
        )

        SettingsItem(
            text = stringResource(Res.string.button_forward),
            subText = "${state.forwardValue}sec",
            painter = painterResource(Res.drawable.fast_forward),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowForwardDialog) }
        )

        SettingsSwitch(
            text = stringResource(Res.string.external_player),
            subText = stringResource(Res.string.watch_on_external_player),
            checked = state.useExternalPlayer,
            painter = painterResource(Res.drawable.ic_player),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onCheckedChange = { sendIntent(SettingsIntent.OnExternalPlayerCheck(it)) }
        )

        SettingsSwitch(
            text = stringResource(Res.string.picture_in_picture),
            checked = state.pipIsEnabled,
            painter = painterResource(Res.drawable.ic_pip),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onCheckedChange = { sendIntent(SettingsIntent.OnEnablePipCheck(it)) }
        )

    }

}

@Composable
fun SettingsTmdbSection(
    sendIntent: (SettingsIntent) -> Unit
) {

    SettingsSection(
        iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    ) { iconColor, bgColor ->

        SettingsItem(
            text = stringResource(Res.string.tmdb_api_token),
            subText = "",
            painter = painterResource(Res.drawable.ic_api),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnTokenTap) }
        )

        SettingsItem(
            text = stringResource(Res.string.how_to_name_files),
            subText = "",
            painter = painterResource(Res.drawable.ic_help),
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
            text = stringResource(Res.string.about),
            subText = stringResource(Res.string.about_desc),
            painter = painterResource(Res.drawable.ic_info),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnAboutTap) }
        )

        SettingsItem(
            text = stringResource(Res.string.make_a_donation),
            subText = stringResource(Res.string.support_me_desc),
            painter = painterResource(Res.drawable.ic_money),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                UriUtils.openWebPage(
                    context = context,
                    url = Constants.CONTACT.SPONSOR
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

        val syncTextColor by animateColorAsState(if (state.fullSyncInProgress) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = .8f))
        SettingsItem(
            text = stringResource(Res.string.sync_library),
            subText = stringResource(if (state.fullSyncInProgress) Res.string.sync_in_progress else Res.string.sync_library_desc),
            valueColor = syncTextColor,
            painter = painterResource(Res.drawable.ic_sync),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.ShowFullSyncDialog(true)) }
        )

        val imagesTextColor by animateColorAsState(if (state.prefetchImagesState is ImagesPrefetchManager.State.InProgress) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = .8f))
        val imagesText = when {
            state.prefetchHdImages && state.prefetchImagesState is ImagesPrefetchManager.State.Idle -> stringResource(Res.string.images_cached)
            state.prefetchHdImages && state.prefetchImagesState is ImagesPrefetchManager.State.InProgress -> {
                val progressState = state.prefetchImagesState as ImagesPrefetchManager.State.InProgress
                stringResource(Res.string.caching_images_in_progress, progressState.progress.times(100).roundToInt())
            }
            else -> stringResource(Res.string.cache_images_desc)
        }
        SettingsSwitch(
            text = stringResource(Res.string.cache_images),
            subText = imagesText,
            checked = state.prefetchHdImages,
            onCheckedChange = { sendIntent(SettingsIntent.OnPrefetchHdImagesCheck(it)) },
            painter = painterResource(Res.drawable.ic_images),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            subTextColor = imagesTextColor
        )

        SettingsItem(
            text = stringResource(Res.string.sources),
            subText = stringResource(Res.string.sources_short_desc),
            painter = painterResource(Res.drawable.ic_folder),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = { sendIntent(SettingsIntent.OnSourcesTap) }
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
            text = stringResource(Res.string.x),
            subText = stringResource(Res.string.stay_informed),
            painter = painterResource(Res.drawable.ic_social_media),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                UriUtils.openWebPage(
                    context = context,
                    url = Constants.CONTACT.X
                )
            }
        )

        SettingsItem(
            text = stringResource(Res.string.source_code),
            subText = "",
            painter = painterResource(Res.drawable.ic_sources),
            iconColor = iconColor,
            iconBackgroundColor = bgColor,
            onTap = {
                UriUtils.openWebPage(
                    context = context,
                    url = Constants.CONTACT.GITHUB
                )
            }
        )

        appVersion?.let {

            SettingsItem(
                text = stringResource(Res.string.app_version),
                subText = it,
                painter = painterResource(Res.drawable.ic_version),
                iconColor = iconColor,
                iconBackgroundColor = bgColor,
                onTap = {
                    UriUtils.openWebPage(
                        context = context,
                        url = Constants.CONTACT.RELEASES
                    )
                }
            )

        }

    }

}