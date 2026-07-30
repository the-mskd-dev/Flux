package com.mskd.flux.screens.setup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.mskd.flux.features.setup.presentation.SetupContrat
import com.mskd.flux.features.setup.presentation.SetupEvent
import com.mskd.flux.features.setup.presentation.SetupIntent
import com.mskd.flux.features.setup.presentation.SetupUiState
import com.mskd.flux.features.setup.presentation.SetupViewModel
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.*
import com.mskd.flux.screens.setup.composables.SetupSourcesContent
import com.mskd.flux.screens.setup.composables.SetupWelcomeContent
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.storagePermissionState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(
    navigate: (Route) -> Unit,
    viewModel: SetupViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = storagePermissionState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SetupEvent.NavigateToSources -> navigate(Sources(fromSetup = true))
                SetupEvent.NavigateToToken -> navigate(Token(fromSetup = true))
                SetupEvent.ShowPermissionDialog -> permissions.launchPermissionRequest()
            }
        }
    }

    if (permissions.status.isGranted) {
        viewModel.handleIntent(SetupIntent.OnPermissionGranted)
    }

    SetupScreenContent(
        state = state,
        sendIntent = { viewModel.handleIntent(it) }
    )

}

@Composable
fun SetupScreenContent(
    state: SetupUiState,
    sendIntent: (SetupIntent) -> Unit
) {

    FluxScaffold(
        title = null,
        onBackTap = null,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sendIntent(SetupIntent.OnNextButton) }
            ) {
                Text.Label.Large("Next")
            }
        }
    ) { _ ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier
                    .sizeIn(maxWidth = 600.dp, maxHeight = 600.dp)
                    .fillMaxSize()
                    .alpha(.15f),
                painter = painterResource(Res.drawable.ic_flux),
                contentDescription = "logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )

            Box(
                modifier = Modifier
                    .padding(FluxUI.Space.medium)
                    .widthIn(max = 600.dp),
            ) {

                Crossfade(
                    targetState = state.screen
                ) { screen ->

                    when (screen) {
                        SetupContrat.Screen.WELCOME -> SetupWelcomeContent()
                        SetupContrat.Screen.SOURCES -> SetupSourcesContent(
                            selectedOption = state.sourcesOption,
                            sendIntent = sendIntent
                        )
                    }

                }

            }

        }


    }

}

@FluxPreview
@Composable
fun SetupScreenContent_Welcome_Preview() {
    FluxThemePreview {
        SetupScreenContent(
            state = SetupUiState(
                screen = SetupContrat.Screen.WELCOME
            ),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun SetupScreenContent_Sources_Preview() {
    FluxThemePreview {
        SetupScreenContent(
            state = SetupUiState(
                screen = SetupContrat.Screen.SOURCES
            ),
            sendIntent = {}
        )
    }
}