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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.navigation.Navigation
import com.kaem.flux.navigation.Route
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
    navigate: (Route) -> Unit,
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
                SettingsEvent.NavigateToAboutScreen -> navigate(Route.About)
                SettingsEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
            }
        }
    }

    SettingsContent(
        state = state,
        context = context,
        appVersion = appVersion,
        sendIntent = viewModel::handleIntent
    )

    state.dialogState?.let {
        SettingsDialog(
            state = it,
            sendIntent = viewModel::handleIntent,
            onDismiss = { viewModel.handleIntent(SettingsIntent.HideDialog) }
        )
    }

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
                    onTap = { sendIntent(SettingsIntent.ShowThemeDialog) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_backward),
                    value = "${state.backwardValue}sec",
                    onTap = { sendIntent(SettingsIntent.ShowBackwardDialog) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_forward),
                    value = "${state.forwardValue}sec",
                    onTap = { sendIntent(SettingsIntent.ShowForwardDialog) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.subtitles_language),
                    value = state.subtitlesLanguage.displayLanguage,
                    onTap = { sendIntent(SettingsIntent.ShowSubtitlesDialog) }
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
    state: SettingsDialogState<T>,
    sendIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedValue by remember { mutableStateOf(state.currentValue) }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { sendIntent(state.applyValue(selectedValue)) },
        title = stringResource(state.title),
        content = {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                state.options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clickable { selectedValue = option.key  }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = selectedValue == option.key,
                            onClick = { selectedValue = option.key }
                        )

                        val value = option.value.second?.let { stringResource(it) } ?: option.value.first
                        Text.Body.Large(
                            modifier = Modifier.weight(1f),
                            text = value.uppercaseFirstLetter(),
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
            state = SettingsDialogState.forward(5),
            sendIntent = {},
            onDismiss = {}
        )
    }
}

