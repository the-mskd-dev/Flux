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

                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    columns = GridCells.Fixed(FluxUI.itemsPerRow.artworks),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + FluxUI.Space.medium,
                        bottom = innerPadding.calculateBottomPadding() + FluxUI.Space.bottomScreen,
                        start = FluxUI.Space.medium,
                        end = FluxUI.Space.medium
                    )
                ) {

                    items(items = state.artworks, key = { it.id }) { artwork ->

                        PrivateArtworkItem(
                            modifier = Modifier.animateItem(),
                            artwork = artwork,
                            sendIntent = sendIntent
                        )

                    }

                }

            }

        }

    }

}

@Composable
fun PrivateArtworkItem(
    modifier: Modifier,
    artwork: Artwork,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        MediaItem(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(FluxUI.Dimension.itemRatio),
            path = artwork.imagePath,
            onClick = { rgb -> sendIntent(PrivateFolderIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            onLongClick = { menuExpanded = true },
            description = artwork.title
        )

        if (menuExpanded) {

            FluxDropDownMenu(
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    FluxDropDownMenuItem(
                        text = stringResource(Res.string.remove_from_private_folder),
                        onClick = {
                            sendIntent(PrivateFolderIntent.RemoveFromPrivateFolder(artwork = artwork))
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = null
                            )
                        }
                    )
                )
            )

        }

    }

}

@Composable
fun PrivatePinGate(
    modifier: Modifier = Modifier,
    pinError: Boolean,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    val pinLength = 4

    var pinInput by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submit(pin: String) {
        if (pin.length != pinLength) return
        sendIntent(PrivateFolderIntent.SubmitPin(pin = pin))
    }

    // The hidden field below keeps the focus forever, so the keyboard never closes
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Wrong pin: the input is reset, the error is only displayed by the fields themselves
    LaunchedEffect(pinError) {
        if (pinError) pinInput = TextFieldValue()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = FluxUI.Space.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(Res.drawable.ic_lock),
            contentDescription = stringResource(Res.string.private_folder),
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.large))

        Text.Content.Body(
            text = stringResource(Res.string.pin_gate_title),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.medium))

        Box {

            Row(
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                repeat(pinLength) { index ->

                    OutlinedTextField(
                        modifier = Modifier
                            .width(40.dp)
                            .focusProperties { canFocus = false },
                        value = pinInput.text.getOrNull(index)?.toString().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        isError = pinError,
                        singleLine = true,
                        textStyle = Text.Style.contentBody().copy(textAlign = TextAlign.Center),
                        visualTransformation = PasswordVisualTransformation()
                    )

                }

            }

            // Hidden field owning the focus: the input is captured there, the fields above only display it
            BasicTextField(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .focusRequester(focusRequester),
                value = pinInput,
                onValueChange = { value ->
                    val digits = value.text.filter { it.isDigit() }.take(pinLength)
                    val normalized = TextFieldValue(text = digits, selection = TextRange(digits.length))

                    if (digits != pinInput.text) {
                        pinInput = normalized

                        // A new input starts: the previous error is no longer relevant
                        if (pinError) sendIntent(PrivateFolderIntent.ClearPinError)

                        submit(pin = digits) // Validates automatically on the last digit
                    } else if (pinInput != normalized) {
                        pinInput = normalized // Keep the caret at the end
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submit(pin = pinInput.text) }
                )
            )

            // Tapping the fields keeps the hidden one focused, so the keyboard stays open
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
            )

        }

    }

}

@FluxPreview
@Composable
fun PrivateScreen_Preview() {
    FluxTheme {
        PrivateScreenContent(
            state = PrivateFolderUiState(locked = false),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun PrivateScreen_Locked_Preview() {
    FluxTheme {
        PrivateScreenContent(
            state = PrivateFolderUiState(locked = true),
            sendIntent = {}
        )
    }
}
