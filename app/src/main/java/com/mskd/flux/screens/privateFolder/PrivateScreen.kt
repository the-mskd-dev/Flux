package com.mskd.flux.screens.privateFolder

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderEvent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderIntent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderUiState
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderViewModel
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.private_folder
import flux.shared.generated.resources.private_folder_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PrivateScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: PrivateFolderViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PrivateFolderEvent.BackToPreviousScreen -> onBack()
                is PrivateFolderEvent.NavigateToMovie -> navigate(Route.Artwork(artworkId = event.artworkId, season = null, rgb = event.rgb))
                is PrivateFolderEvent.NavigateToShow -> navigate(Route.Show(artworkId = event.artworkId, rgb = event.rgb))
            }
        }
    }

    PrivateScreenContent(
        state = uiState,
        sendIntent = viewModel::handleIntent
    )

}

@Composable
fun PrivateScreenContent(
    state: PrivateFolderUiState,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(Res.string.private_folder),
        onBackTap = { sendIntent(PrivateFolderIntent.OnBackTap) }
    ) { innerPadding ->

        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = state.locked,
            label = "PrivateFolderLockState"
        ) { locked ->

            if (locked) {

                PrivatePinGate(
                    modifier = Modifier.padding(innerPadding),
                    isError = state.pinError,
                    sendIntent = sendIntent
                )

            } else if (state.artworks.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .systemBarsPadding(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text.Content.Body(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        text = stringResource(Res.string.private_folder_empty),
                        textAlign = TextAlign.Center
                    )
                }

            } else {

                PrivateContentGrid(
                    state = state,
                    innerPadding = innerPadding,
                    sendIntent = sendIntent
                )

            }

        }

    }

}