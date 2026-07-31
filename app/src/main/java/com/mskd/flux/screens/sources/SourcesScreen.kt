package com.mskd.flux.screens.sources

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.sources.presentation.SourcesContent
import com.mskd.flux.features.sources.presentation.SourcesDialog
import com.mskd.flux.features.sources.presentation.SourcesEvent
import com.mskd.flux.features.sources.presentation.SourcesIntent
import com.mskd.flux.features.sources.presentation.SourcesViewModel
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.navigation.Route
import com.mskd.flux.presentations.components.rememberSafFolderPicker
import com.mskd.flux.screens.sources.composables.items.CustomSourceItem
import com.mskd.flux.screens.sources.composables.DeleteSourceDialog
import com.mskd.flux.screens.sources.composables.SourcesInformationDialog
import com.mskd.flux.screens.sources.composables.items.SystemSourceItem
import com.mskd.flux.screens.sources.composables.sourcesAnnotatedString
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.FluxButton
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.groupedShape
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_source
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.next
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.sources
import flux.shared.generated.resources.sources_full_desc
import flux.shared.generated.resources.sources_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SourcesScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    fromSetup: Boolean,
    viewModel: SourcesViewModel = koinViewModel(parameters = { parametersOf(fromSetup) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pickFolder = rememberSafFolderPicker { uri ->
        viewModel.handleIntent(SourcesIntent.SaveFolder(uri.toString()))
    }

    BackHandler(true) {
        viewModel.handleIntent(SourcesIntent.OnBackTap)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SourcesEvent.BackToPreviousScreen -> onBack()
                SourcesEvent.OpenFolderSelection -> pickFolder()
                SourcesEvent.NavigateToCatalog -> navigate(Route.Catalog)
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
                    dialog = uiState.dialog,
                    sendIntent = { viewModel.handleIntent(intent = it) }
                )
            }
        }

    }

    // TODO: Delete in October 2026
    if (uiState.dialog is SourcesDialog.NewFeatureInformation) {
        SourcesInformationDialog(
            sendIntent = { viewModel.handleIntent(intent = it) }
        )
    }

}

@Composable
fun SourcesScreenContent(
    content: SourcesContent,
    dialog: SourcesDialog? = null,
    sendIntent: (SourcesIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(Res.string.sources),
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onBackTap = if (content.fromSetup) null else {
            { sendIntent(SourcesIntent.OnBackTap) }
        },
        floatingActionButton = {
            if (content.fromSetup) {
                ExtendedFloatingActionButton(
                    onClick = { sendIntent(SourcesIntent.OnNextTap) }
                ) {
                    Text.Label.Large(stringResource(Res.string.next))
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }


            item {
                Text.Title.Large(
                    modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                    text = stringResource(Res.string.sources_title)
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.medium))
            }


            item {
                Text.Annotated(
                    modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                    text = sourcesAnnotatedString(Res.string.sources_full_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.medium))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(FluxUI.Space.medium)
                        .clip(RoundedCornerShape(FluxUI.shapes.list)),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.listItem)
                ) {
                    SystemSourceItem(name = stringResource(Res.string.movies))
                    SystemSourceItem(name = stringResource(Res.string.downloads))
                }
            }

            item {
                FluxButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = FluxUI.Space.medium),
                    shape = MaterialTheme.shapes.extraLarge,
                    text = stringResource(Res.string.add_source)
                ) { sendIntent(SourcesIntent.OpenFolderSelection) }
            }

            itemsIndexed(
                items = content.folders,
                key = { _, folder -> folder.path },
            ) { index, folder ->

                CustomSourceItem(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = FluxUI.Space.medium)
                        .groupedShape(
                            index = index,
                            lastIndex = content.folders.lastIndex
                        ),
                    folder = folder,
                    onDelete = { sendIntent(SourcesIntent.ShowDeleteDialog(folder)) }
                )

                if (index != content.folders.lastIndex)
                    Spacer(Modifier.height(FluxUI.Space.listItem))

            }

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + FluxUI.Space.large))
            }

        }

    }

    (dialog as? SourcesDialog.ConfirmDelete)?.let {
        DeleteSourceDialog(
            folder = it.folder,
            sendIntent = sendIntent
        )
    }

}

@Composable
@FluxPreview
fun SourcesScreenContent_Preview() {
    FluxThemePreview {
        SourcesScreenContent(
            content = SourcesContent(
                folders = FilesMockups.userFolders,
                fromSetup = true
            )
        ) { }
    }
}