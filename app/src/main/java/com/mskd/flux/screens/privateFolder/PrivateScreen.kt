package com.mskd.flux.screens.privateFolder

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderEvent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderIntent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderUiState
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderViewModel
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.ic_lock
import flux.shared.generated.resources.pin_gate_title
import flux.shared.generated.resources.private_folder
import flux.shared.generated.resources.private_folder_empty
import flux.shared.generated.resources.remove_from_private_folder
import org.jetbrains.compose.resources.painterResource
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
                    pinError = state.pinError,
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