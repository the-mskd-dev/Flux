package com.mskd.flux.screens.token

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.navigation.Route
import com.mskd.flux.ui.component.FluxIconButton
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.FluxTextButton
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.buildLinkedString
import kotlinx.coroutines.launch

@Composable
fun TokenScreen(
    onBack: () -> Unit,
    navigate: (Route) -> Unit,
    fromSettings: Boolean,
    viewModel: TokenViewModel = hiltViewModel<TokenViewModel, TokenViewModel.Factory>(
        creationCallback = { factory -> factory.create(fromSettings) }
    )
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                TokenEvent.BackToPreviousScreen -> onBack()
                TokenEvent.NavigateToHomeScreen -> navigate(Route.Library)
            }
        }
    }

    BackHandler(true) {
        if (fromSettings) onBack()
    }

    TokenScreenContent(
        state = uiState,
        sendIntent = viewModel::handleIntent
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TokenScreenContent(
    state: TokenUiState,
    sendIntent: (TokenIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.tmdb_api_key),
        onBackTap = if (state.showBackButton) { { sendIntent(TokenIntent.OnBackTap) } } else null,
        floatingActionButton = {

            AnimatedVisibility(
                visible = !state.showBackButton,
                enter = Ui.Animation.buttonEnter,
                exit = Ui.Animation.buttonExit
            ) {

                FluxTextButton(
                    stringResource(R.string.continue_without_api_key),
                    onTap = { sendIntent(TokenIntent.OnCancelTap) }
                )

            }

        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = Ui.Space.MEDIUM)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            TokenDescription()

            TokenTutorial()

            Column {

                TokenInput(
                    token = state.token,
                    isLoading = state.isLoading,
                    sendIntent = sendIntent
                )

                AnimatedVisibility(
                    modifier = Modifier.padding(start = Ui.Space.EXTRA_SMALL, top = Ui.Space.SMALL),
                    visible = state.message != TokenMessage.None,
                ) {

                    when (state.message) {
                        TokenMessage.Success -> Text.Label.Small(
                            text = stringResource(R.string.token_validated),
                            color = MaterialTheme.colorScheme.primary
                        )
                        TokenMessage.Error -> Text.Label.Small(
                            text = stringResource(R.string.token_error),
                            color = MaterialTheme.colorScheme.error
                        )
                        TokenMessage.None -> {}
                    }

                }
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@Composable
fun TokenDescription() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        horizontalAlignment = Alignment.Start
    ) {

        Text.Annotated(
            text = buildLinkedString(
                template = stringResource(R.string.token_desc_1),
                stringResource(R.string.tmdb) to Constants.TMDB.WEBSITE
            )
        )

        Text.Body.Large(
            text = stringResource(R.string.token_desc_2)
        )

        Text.Body.Large(
            text = stringResource(R.string.token_desc_3)
        )

    }

}

@Composable
fun TokenTutorial() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        horizontalAlignment = Alignment.Start
    ) {


        Row {

            Text.Body.Large(text = "1. ")

            Text.Annotated(
                text = buildLinkedString(
                    template = stringResource(R.string.token_tutorial_step_1),
                    stringResource(R.string.log_in) to Constants.TMDB.LOG_IN,
                    stringResource(R.string.sign_up) to Constants.TMDB.SIGN_UP,
                )
            )

        }

        Row {

            Text.Body.Large(text = "2. ")

            Text.Annotated(
                text = buildLinkedString(
                    template = stringResource(R.string.token_tutorial_step_2),
                    stringResource(R.string.api_key) to Constants.TMDB.GET_API_KEY,
                )
            )

        }


    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TokenInput(
    token: String,
    isLoading: Boolean,
    sendIntent: (TokenIntent) -> Unit
) {

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .widthIn(max = 700.dp)
            .padding(top = Ui.Space.LARGE),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier
                .weight(.85f)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focus ->

                    if (focus.isFocused) {
                        scope.launch { bringIntoViewRequester.bringIntoView() }
                    }

                },
            value = token,
            onValueChange = { sendIntent(TokenIntent.SetToken(it)) },
            singleLine = true,
            shape = Ui.Shape.Corner.Small,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(stringResource(R.string.token_input)) },
            trailingIcon = {
                if (token.isNotEmpty()) {
                    IconButton(
                        modifier = Modifier.size(18.dp),
                        onClick = { sendIntent(TokenIntent.SetToken("")) },
                        content = { Icon(imageVector = Icons.Rounded.Clear, contentDescription = "clear button") }
                    )
                }
            }
        )

        Crossfade(
            modifier = Modifier
                .height(TextFieldDefaults.MinHeight)
                .weight(.15f),
            targetState = isLoading
        ) { loading ->

            when (loading) {
                true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
                false -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        FluxIconButton(
                            imageVector = Icons.Default.Check,
                            onTap = {
                                keyboardController?.hide()
                                sendIntent(TokenIntent.SaveToken)
                            },
                            contentDescription = "validate token button"
                        )

                    }
                }
            }

        }

    }

}

@FluxPreview
@Composable
fun TokenScreen_Preview() {
    AppTheme {
        TokenScreenContent(
            state = TokenUiState(
                token = "azERTyuiOQSdfghJKLmwxCvbn",
                showBackButton = false,
                isLoading = false,
                message = TokenMessage.Success
            ),
            sendIntent = {},
        )
    }
}
