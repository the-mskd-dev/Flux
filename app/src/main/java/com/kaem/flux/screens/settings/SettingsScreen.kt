package com.kaem.flux.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.component.TextBodyLarge
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.WebLink
import com.kaem.flux.utils.extensions.uppercaseFirstLetter

@Composable
fun SettingsScreen(
    onBackButtonTap: () -> Unit,
    navigateToHowToScreen: () -> Unit,
    navigateToAboutScreen: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appVersion = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        FluxTopBar(
            text = stringResource(R.string.settings),
            onBackButtonTap = onBackButtonTap
        )

        SettingsSection {

            SettingsItem(
                text = stringResource(R.string.app_theme),
                value = stringResource(state.uiTheme.stringResourceId),
                onTap = { viewModel.showUiThemeDialog(true) }
            )

            SettingsDivider()

            SettingsItem(
                text = stringResource(R.string.button_backward),
                value = "${state.backwardValue}sec",
                onTap = { viewModel.showBackwardDialog(true) }
            )

            SettingsDivider()

            SettingsItem(
                text = stringResource(R.string.button_forward),
                value = "${state.forwardValue}sec",
                onTap = { viewModel.showForwardDialog(true) }
            )

            SettingsDivider()

            SettingsItem(
                text = stringResource(R.string.subtitles_language),
                value = state.subtitlesLanguage.displayLanguage,
                onTap = { viewModel.showSubtitlesLanguageDialog(true) }
            )

        }

        SettingsSection {

            SettingsItem(
                text = stringResource(R.string.how_to_name_files),
                value = "",
                onTap = navigateToHowToScreen
            )

            SettingsDivider()

            SettingsItem(
                text = stringResource(R.string.about),
                value = "",
                onTap = navigateToAboutScreen
            )

            SettingsDivider()

            SettingsItem(
                text = stringResource(R.string.make_a_donation),
                value = "",
                onTap = {
                    WebLink.openPage(
                        context = context,
                        url = "https://paypal.me/kevynbct"
                    )
                }
            )

        }

        appVersion?.let {

            SettingsSection {

                SettingsItem(
                    text = stringResource(R.string.app_version),
                    value = it,
                    onTap = {}
                )

            }

        }

        Spacer(modifier = Modifier.navigationBarsPadding())

    }

    SettingsDialog(
        show = state.showBackwardDialog,
        currentValue = state.backwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.setBackwardValue(it) },
        onDismiss = { viewModel.showBackwardDialog(false) }
    )

    SettingsDialog(
        show = state.showForwardDialog,
        currentValue = state.forwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.setForwardValue(it) },
        onDismiss = { viewModel.showForwardDialog(false) }
    )

    SettingsDialog(
        show = state.showUiThemeDialog,
        currentValue = state.uiTheme,
        options = mapOf(
            Ui.THEME.LIGHT to stringResource(Ui.THEME.LIGHT.stringResourceId),
            Ui.THEME.DARK to stringResource(Ui.THEME.DARK.stringResourceId),
            Ui.THEME.SYSTEM to stringResource(Ui.THEME.SYSTEM.stringResourceId),
        ),
        onSelect = { viewModel.setUiTheme(it) },
        onDismiss = { viewModel.showUiThemeDialog(false) }
    )

    SettingsDialog(
        show = state.showSubtitlesLanguage,
        currentValue = state.subtitlesLanguage,
        options = SettingsViewModel.languages,
        onSelect = { viewModel.setSubtitlesLanguage(it) },
        onDismiss = { viewModel.showSubtitlesLanguageDialog(false) }
    )

}

@Composable
fun SettingsSection(content: @Composable () -> Unit) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Ui.Space.MEDIUM)
                .clip(Ui.Shape.RoundedCorner)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = Ui.Space.MEDIUM),
            horizontalAlignment = Alignment.Start
        ) { content() }
}

@Composable
fun SettingsItem(
    text: String,
    value: String,
    onTap: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable { onTap() }
            .fillMaxWidth()
            .padding(vertical = Ui.Space.MEDIUM),
    ) {

        TextBodyLarge(
            text = text,
            fontSize = Ui.FontSize.LARGE
        )

        TextBodyLarge(
            text = value.uppercaseFirstLetter(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
        )

    }

}

@Composable
fun SettingsDivider() {

    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    )

}

@Composable
fun <T> SettingsDialog(
    show: Boolean,
    currentValue: T,
    options: Map<T, String>,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        show = show,
        hideButtons = true,
        onDismissRequest = onDismiss,
        content = {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clickable { onSelect(option.key); onDismiss()  }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = currentValue == option.key,
                            onClick = { onSelect(option.key); onDismiss() }
                        )

                        TextBodyLarge(
                            modifier = Modifier.weight(1f),
                            text = option.value.uppercaseFirstLetter(),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }

            }

        }
    )

}