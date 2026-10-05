package com.mskd.flux.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.features.settings.presentation.SettingsEvent
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.features.settings.presentation.SettingsUiState
import com.mskd.flux.features.settings.presentation.SettingsViewModel
import com.mskd.flux.features.settings.ui.composables.SettingsAppInfoSection
import com.mskd.flux.features.settings.ui.composables.SettingsCustomizationSection
import com.mskd.flux.features.settings.ui.composables.SettingsDialogs
import com.mskd.flux.features.settings.ui.composables.SettingsOtherSection
import com.mskd.flux.features.settings.ui.composables.SettingsPlayerSection
import com.mskd.flux.features.settings.ui.composables.SettingsPrivateFolderDialogs
import com.mskd.flux.features.settings.ui.composables.SettingsPrivateFolderSection
import com.mskd.flux.features.settings.ui.composables.SettingsSyncSection
import com.mskd.flux.features.settings.ui.composables.SettingsTmdbSection
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Route.Token
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.FluxOptionsDialog
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.ui.theme.LocalUiGlobal
import com.mskd.flux.utils.rememberNotificationsPermission
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navigate: (Route) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationsPermission = rememberNotificationsPermission()


    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SettingsEvent.BackToPreviousScreen -> onBack()
                SettingsEvent.NavigateToTokenScreen -> navigate(Token(fromSetup = false))
                SettingsEvent.NavigateToAboutScreen -> navigate(Route.About)
                SettingsEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
                SettingsEvent.NavigateToCustomizationScreen -> navigate(Route.Customization)
                SettingsEvent.NavigateToSourcesScreen -> navigate(Route.Sources())
                SettingsEvent.RequestExternalPlayerPermission -> notificationsPermission?.request()
                SettingsEvent.PrivateFolderPinUpdated -> {}
            }
        }
    }

    SettingsContent(
        state = state,
        sendIntent = viewModel::handleIntent
    )

    state.optionsDialog?.let { dialogState ->
        FluxOptionsDialog(
            state = dialogState,
            onValidate = { viewModel.handleIntent(it) },
            onDismiss = { viewModel.handleIntent(SettingsIntent.HideDialog) }
        )
    }

    SettingsDialogs(
        dialog = state.settingsDialog,
        sendIntent = viewModel::handleIntent
    )

    SettingsPrivateFolderDialogs(
        state = state,
        sendIntent = viewModel::handleIntent
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(Res.string.settings),
        onBackTap = { sendIntent(SettingsIntent.OnBackTap) },
        showBackButton = LocalUiGlobal.current.navigationStyle == NavigationStyle.TOP_BAR
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            SettingsCustomizationSection(
                state = state,
                sendIntent = sendIntent
            )

            SettingsPlayerSection(
                state = state,
                sendIntent = sendIntent
            )

            SettingsPrivateFolderSection(
                state = state,
                sendIntent = sendIntent
            )

            SettingsTmdbSection(
                sendIntent = sendIntent
            )

            SettingsOtherSection(
                sendIntent = sendIntent
            )

            SettingsSyncSection(
                state = state,
                sendIntent = sendIntent
            )

            SettingsAppInfoSection(
                appVersion = state.appVersion,
                sendIntent = sendIntent
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + FluxUI.Space.bottomScreen))

        }

    }

}

@FluxPreview
@Composable
fun SettingsScreen_Preview() {
    FluxTheme {
        SettingsContent(
            state = SettingsUiState(appVersion = "6.1.6"),
        ) { }
    }
}

