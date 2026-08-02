package com.mskd.flux.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
import com.mskd.flux.features.setup.domain.model.SetupScreen
import com.mskd.flux.features.setup.presentation.SetupEvent
import com.mskd.flux.features.setup.presentation.SetupIntent
import com.mskd.flux.features.setup.presentation.SetupUiState
import com.mskd.flux.features.setup.presentation.SetupViewModel
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Sources
import com.mskd.flux.navigation.Route.Token
import com.mskd.flux.screens.setup.composables.SetupSourcesContent
import com.mskd.flux.screens.setup.composables.SetupWelcomeContent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import com.mskd.flux.utils.storagePermissionState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import flux.shared.generated.resources.next
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(
    navigate: (Route) -> Unit,
    viewModel: SetupViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = storagePermissionState { isGranted ->
        if (isGranted)
            viewModel.handleIntent(SetupIntent.OnPermissionGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SetupEvent.NavigateToSources -> navigate(Sources(fromSetup = true))
                SetupEvent.NavigateToToken -> navigate(Token(fromSetup = true))
                SetupEvent.ShowPermissionDialog -> {
                    if (permissions.status.isGranted) viewModel.handleIntent(SetupIntent.OnPermissionGranted)
                    else permissions.launchPermissionRequest()
                }
            }
        }
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

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxSize(),
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
                .fillMaxWidthWithLimit(),
        ) {

            AnimatedContent(
                targetState = state.screen
            ) { screen ->

                when (screen) {
                    SetupScreen.WELCOME -> SetupWelcomeContent()
                    SetupScreen.SOURCES -> SetupSourcesContent(
                        systemFoldersEnabled = state.systemFoldersEnabled,
                        sendIntent = sendIntent
                    )
                }

            }

        }

        ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(all = FluxUI.Space.medium)
                .navigationBarsPadding()
            ,
            onClick = { sendIntent(SetupIntent.OnNextButton) }
        ) {
            Text.Button(stringResource(Res.string.next))
        }

    }

}

@FluxPreview
@Composable
fun SetupScreenContent_Welcome_Preview() {
    FluxThemePreview {
        SetupScreenContent(
            state = SetupUiState(
                screen = SetupScreen.WELCOME
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
                screen = SetupScreen.SOURCES
            ),
            sendIntent = {}
        )
    }
}