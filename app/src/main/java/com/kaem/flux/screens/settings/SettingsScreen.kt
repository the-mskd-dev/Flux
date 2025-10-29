package com.kaem.flux.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.Navigation.Navigation
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.WebLink
import com.kaem.flux.utils.extensions.uppercaseFirstLetter

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navigate: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appVersion = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SettingsEvent.BackToPreviousScreen -> onBack()
                SettingsEvent.NavigateToAboutScreen -> navigate(Navigation.ABOUT.build())
                SettingsEvent.NavigateToHowToScreen -> navigate(Navigation.HOW_TO.build())
            }
        }
    }

    SettingsContent(
        state = state,
        context = context,
        appVersion = appVersion,
        sendIntent = viewModel::handleIntent
    )

    SettingsDialog(
        show = state.showBackwardDialog,
        currentValue = state.backwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.handleIntent(SettingsIntent.SetBackwardValue(it)) },
        onDismiss = { viewModel.handleIntent(SettingsIntent.BackwardDialog(false)) }
    )

    SettingsDialog(
        show = state.showForwardDialog,
        currentValue = state.forwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.handleIntent(SettingsIntent.SetForwardValue(it)) },
        onDismiss = { viewModel.handleIntent(SettingsIntent.ForwardDialog(false)) }
    )

    SettingsDialog(
        show = state.showUiThemeDialog,
        currentValue = state.uiTheme,
        options = mapOf(
            //Ui.THEME.LIGHT to stringResource(Ui.THEME.LIGHT.stringResourceId),
            Ui.THEME.DARK to stringResource(Ui.THEME.DARK.stringResourceId),
            Ui.THEME.SYSTEM to stringResource(Ui.THEME.SYSTEM.stringResourceId),
        ),
        onSelect = { viewModel.handleIntent(SettingsIntent.SetThemeValue(it)) },
        onDismiss = { viewModel.handleIntent(SettingsIntent.ThemeDialog(false)) }
    )

    SettingsDialog(
        show = state.showSubtitlesLanguage,
        currentValue = state.subtitlesLanguage,
        options = SettingsViewModel.languages,
        onSelect = { viewModel.handleIntent(SettingsIntent.SetSubtitlesValue(it)) },
        onDismiss = { viewModel.handleIntent(SettingsIntent.SubtitlesDialog(false)) }
    )

}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    context: Context,
    appVersion: String?,
    sendIntent: (SettingsIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.settings),
        onBackTap = { sendIntent(SettingsIntent.OnBackTap) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            SettingsSection {

                SettingsItem(
                    text = stringResource(R.string.app_theme),
                    value = stringResource(state.uiTheme.stringResourceId),
                    onTap = { sendIntent(SettingsIntent.ThemeDialog(true)) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_backward),
                    value = "${state.backwardValue}sec",
                    onTap = { sendIntent(SettingsIntent.BackwardDialog(true)) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_forward),
                    value = "${state.forwardValue}sec",
                    onTap = { sendIntent(SettingsIntent.ForwardDialog(true)) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.subtitles_language),
                    value = state.subtitlesLanguage.displayLanguage,
                    onTap = { sendIntent(SettingsIntent.SubtitlesDialog(true)) }
                )

            }

            SettingsSection {

                SettingsItem(
                    text = stringResource(R.string.how_to_name_files),
                    value = "",
                    onTap = { sendIntent(SettingsIntent.OnHowToTap) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.about),
                    value = "",
                    onTap = { sendIntent(SettingsIntent.OnAboutTap) }
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

            //Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@Composable
fun SettingsSection(content: @Composable () -> Unit) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Ui.Space.MEDIUM)
                .clip(Ui.Shape.Corner.Small)
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

        Text.Title.Medium(
            text = text,
        )

        Text.Title.Small(
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

                        Text.Body.Large(
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

@Preview
@Composable
fun SettingsScreen_Preview() {
    FluxTheme {
        SettingsContent(
            state = SettingsUiState(),
            context = LocalContext.current,
            appVersion = "1.0.0",
        ) { }
    }
}

@Preview
@Composable
fun SettingsDialog_Preview() {
    FluxTheme {
        SettingsDialog(
            show = true,
            currentValue = SettingsViewModel.playerSeconds.keys.first(),
            options = SettingsViewModel.playerSeconds,
            onSelect = { },
            onDismiss = { }
        )
    }
}

