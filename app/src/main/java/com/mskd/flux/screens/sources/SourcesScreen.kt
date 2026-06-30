package com.mskd.flux.screens.sources

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.model.core.presentation.State
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Artwork
import com.mskd.flux.screen.show.ShowEvent
import com.mskd.flux.screen.show.ShowIntent
import com.mskd.flux.screen.show.ShowViewModel
import com.mskd.flux.screen.sources.SourcesContent
import com.mskd.flux.screen.sources.SourcesEvent
import com.mskd.flux.screen.sources.SourcesIntent
import com.mskd.flux.screen.sources.SourcesViewModel
import com.mskd.flux.screen.token.TokenIntent
import com.mskd.flux.screens.sources.composables.FolderItem
import com.mskd.flux.screens.sources.composables.PermanentFolderItem
import com.mskd.flux.screens.sources.composables.UserFolderItem
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.FluxTextButton
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.WebLink
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.skip
import flux.shared.generated.resources.tmdb_api_token
import flux.shared.generated.resources.token_desc_2
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SourcesScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: SourcesViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SourcesEvent.BackToPreviousScreen -> onBack()
                SourcesEvent.OpenFolderSelection -> {
                    Trace.debug(message = "Open Folder selection")
                }
            }
        }
    }

    AnimatedContent(
        targetState = uiState.state,
        label = "SourcesScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(SourcesIntent.OnBackTap) }
                )
            }
            is State.Content<SourcesContent> -> {

                SourcesScreenContent(
                    content = state.content,
                    sendIntent = { viewModel.handleIntent(intent = it) }
                )
            }
        }

    }

}

@Composable
fun SourcesScreenContent(
    content: SourcesContent,
    sendIntent: (SourcesIntent) -> Unit
) {

    FluxScaffold(
        title = "Sources",
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onBackTap = { sendIntent(SourcesIntent.OnBackTap) },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        ) {

            item { Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding())) }

            item {
                Text.Body.Large(
                    text = "Description"
                )
            }

            item { PermanentFolderItem(name = stringResource(Res.string.movies)) }
            item { PermanentFolderItem(name = stringResource(Res.string.downloads)) }

            items(items = content.folders) { folder ->

                UserFolderItem(folder = folder)

            }

            item { Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + FluxUI.Space.large)) }

        }

    }

}

@Composable
@FluxPreview
fun SourcesScreenContent_Preview() {
    FluxTheme {
        SourcesScreenContent(
            content = SourcesContent(
                folders = FilesMockups.userFolders
            )
        ) { }
    }
}