package com.mskd.flux.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.features.settings.presentation.SettingsEvent
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.features.settings.presentation.SettingsUiState
import com.mskd.flux.features.settings.presentation.SettingsViewModel
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Route.Token
import com.mskd.flux.screens.settings.composables.SettingsAppInfoSection
import com.mskd.flux.screens.settings.composables.SettingsCustomizationSection
import com.mskd.flux.screens.settings.composables.SettingsOtherSection
import com.mskd.flux.screens.settings.composables.SettingsPlayerSection
import com.mskd.flux.screens.settings.composables.SettingsSyncSection
import com.mskd.flux.screens.settings.composables.SettingsTmdbSection
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.FluxOptionsDialog
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.notificationsPermissionState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.settings
import flux.shared.generated.resources.sync_library
import flux.shared.generated.resources.sync_library_dialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navigate: (Route) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationsPermission = notificationsPermissionState()
    val context = LocalContext.current
    val appVersion = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName


    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SettingsEvent.BackToPreviousScreen -> onBack()
                SettingsEvent.NavigateToTokenScreen -> navigate(Token(fromSetup = false))
                SettingsEvent.NavigateToAboutScreen -> navigate(Route.About)
                SettingsEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
                SettingsEvent.NavigateToCustomizationScreen -> navigate(Route.Customization)
                SettingsEvent.NavigateToSourcesScreen -> navigate(Route.Sources())
                SettingsEvent.RequestExternalPlayerPermission -> notificationsPermission?.launchPermissionRequest()
            }
        }
    }

    SettingsContent(
        state = state,
        context = context,
        appVersion = appVersion,
        sendIntent = viewModel::handleIntent
    )

    state.dialogState?.let { dialogState ->
        FluxOptionsDialog(
            state = dialogState,
            onValidate = { viewModel.handleIntent(it) },
            onDismiss = { viewModel.handleIntent(SettingsIntent.HideDialog) }
        )
    }

    if (state.showSyncDialog) {
        SettingsFullSyncDialog(
            sendIntent = viewModel::handleIntent,
            onDismiss = { viewModel.handleIntent(SettingsIntent.ShowFullSyncDialog(show = false)) }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    context: Context,
    appVersion: String?,
    sendIntent: (SettingsIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(Res.string.settings),
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onBackTap = { sendIntent(SettingsIntent.OnBackTap) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
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

            SettingsTmdbSection(
                sendIntent = sendIntent
            )

            SettingsOtherSection(
                context = context,
                sendIntent = sendIntent
            )

            SettingsSyncSection(
                state = state,
                sendIntent = sendIntent
            )

            SettingsAppInfoSection(
                context = context,
                appVersion = appVersion
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + FluxUI.Space.bottomScreen))

        }

    }

}

@Composable
fun SettingIcon(
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    contentDescription: String
) {

    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(all = FluxUI.Space.small),
        painter = painter,
        tint = iconColor,
        contentDescription = contentDescription
    )

}

@Composable
fun SettingsFullSyncDialog(
    sendIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { sendIntent(SettingsIntent.ProceedFullSync) },
        title = stringResource(Res.string.sync_library),
        content = {
            Text.Content.Body(text = stringResource(Res.string.sync_library_dialog))
        }
    )

}

@FluxPreview
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

