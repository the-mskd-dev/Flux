package com.kaem.flux.screens.token

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview
import kotlinx.coroutines.launch

@Composable
fun TokenScreen(
    onBack: () -> Unit,
    fromSettings: Boolean,
    viewModel: TokenViewModel = hiltViewModel<TokenViewModel, TokenViewModel.Factory>(
        creationCallback = { factory -> factory.create(fromSettings) }
    )
) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                TokenEvent.BackToPreviousScreen -> onBack()
                TokenEvent.TokenValidated -> {

                    val result = snackbarHostState.showSnackbar(
                        message = "Token sauvegardé",
                        withDismissAction = true
                    )

                    if (result == SnackbarResult.Dismissed) {
                        onBack()
                    }

                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TokenUiEffect.TokenError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Le token n'est pas valide",
                            withDismissAction = true
                        )
                    }
                }
            }
        }
    }

    BackHandler(true) {
        if (fromSettings) onBack()
    }

    TokenScreenContent(
        state = uiState,
        snackbarHostState = snackbarHostState,
        sendIntent = viewModel::handleIntent
    )

}

@Composable
fun TokenScreenContent(
    state: TokenUiState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (TokenIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.tmdb_api_key),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onBackTap = if (state.showBackButton) { { sendIntent(TokenIntent.OnBackTap) } } else null
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Ui.Space.MEDIUM)
                .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TokenDescription()

            TokenTutorial()

            TokenInput(
                token = state.token,
                isLoading = state.isLoading,
                sendIntent = sendIntent
            )

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

        Text.Body.Large(
            text = "Dans le but d'être une application complètement FOSS (Free and Open-Source Software), et de garantir un meilleur contrôle des données, la clé d'API pour le service TheMovieDB n'est pas fournie.",
        )

        Text.Body.Large(
            text = "La clé d'API étant essentielle pour le bon fonctionnement de l'application, il est nécessaire que vous fournissiez votre propre clé."
        )

        Text.Body.Large(
            text = "Pour cela, veuillez suivre la démarche suivante :"
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

        Text.Annotated(
            text = buildAnnotatedString {
                append("1. ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://www.themoviedb.org/login?to=read_me&redirect=%2Fdocs%2Fgetting-started"
                    )
                ) {
                    append("Connectez vous")
                }
                append(" ou ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://www.themoviedb.org/signup"
                    )
                ) {
                    append("créez un compte")
                }
                append(" sur le service TMDB.")
            }
        )

        Text.Annotated(
            text = buildAnnotatedString {
                append("2. Récupérez votre ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://www.themoviedb.org/settings/api"
                    )
                ) {
                    append("clé d'API")
                }
                append(" et copiez-la dans le champ ci-dessous.")
            }
        )

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TokenInput(
    token: String,
    isLoading: Boolean,
    sendIntent: (TokenIntent) -> Unit
) {

    Row(
        modifier = Modifier.widthIn(max = 700.dp).padding(top = Ui.Space.LARGE),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(.85f),
            value = token,
            onValueChange = { sendIntent(TokenIntent.SetToken(it)) },
            singleLine = true,
            shape = Ui.Shape.Corner.Small,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text("Saisir votre clé d'API") },
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
                        IconButton(
                            modifier =
                                Modifier.size(
                                    IconButtonDefaults.mediumContainerSize(
                                        IconButtonDefaults.IconButtonWidthOption.Wide
                                    )
                                ),
                            onClick = { sendIntent(TokenIntent.SaveToken) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = IconButtonDefaults.mediumSquareShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "validate token button"
                            )
                        }
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
                showBackButton = true,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            sendIntent = {},
        )
    }
}
