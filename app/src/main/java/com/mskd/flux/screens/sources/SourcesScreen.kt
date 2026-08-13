package com.mskd.flux.screens.sources

import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.sources.presentation.SourcesContent
import com.mskd.flux.features.sources.presentation.SourcesEvent
import com.mskd.flux.features.sources.presentation.SourcesIntent
import com.mskd.flux.features.sources.presentation.SourcesViewModel
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.presentations.components.rememberSafFolderPicker
import com.mskd.flux.screens.sources.composables.SourcesInformationDialog
import com.mskd.flux.screens.sources.composables.items.CustomSourceItem
import com.mskd.flux.screens.sources.composables.items.SystemSourceItem
import com.mskd.flux.screens.sources.composables.sourcesAnnotatedString
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.storagePermissionState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_source
import flux.shared.generated.resources.folder_deleted
import flux.shared.generated.resources.ic_add
import flux.shared.generated.resources.next
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.sources
import flux.shared.generated.resources.system_folders
import flux.shared.generated.resources.system_folders_toggle_desc
import flux.shared.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SourcesScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    fromSetup: Boolean,
    viewModel: SourcesViewModel = koinViewModel(parameters = { parametersOf(fromSetup) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissions = storagePermissionState { isGranted ->
        if (isGranted)
            viewModel.handleIntent(SourcesIntent.OnPermissionGranted)
    }


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
                SourcesEvent.NavigateToToken -> navigate(Route.Token(fromSetup = true))
                SourcesEvent.ShowPermissionDialog -> {
                    if (permissions.status.isGranted) {
                        viewModel.handleIntent(SourcesIntent.OnPermissionGranted)
                    } else {
                        permissions.launchPermissionRequest()
                    }
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
                    onBackButtonClick = { viewModel.handleIntent(SourcesIntent.OnBackTap) }
                )
            }
            is State.Content<SourcesContent> -> {

                SourcesScreenContent(
                    content = state.content,
                    snackbarHostState = snackbarHostState,
                    sendIntent = { viewModel.handleIntent(intent = it) }
                )

            }
        }

    }

    // TODO: Delete in October 2026
    if (uiState.showFeatureDialog) {
        SourcesInformationDialog(
            sendIntent = { viewModel.handleIntent(intent = it) }
        )
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SourcesScreenContent(
    content: SourcesContent,
    snackbarHostState: SnackbarHostState? = null,
    sendIntent: (SourcesIntent) -> Unit
) {

    val folderDeleted = stringResource(Res.string.folder_deleted)
    val undo = stringResource(Res.string.undo)

    LaunchedEffect(content.waitingDeleteFolder) {
        if (content.waitingDeleteFolder != null) {
            snackbarHostState?.showSnackbar(
                message = folderDeleted,
                actionLabel = undo,
                duration = SnackbarDuration.Short
            )?.let { result ->
                when (result) {
                    SnackbarResult.Dismissed -> sendIntent(SourcesIntent.FinalizeDelete)
                    SnackbarResult.ActionPerformed -> sendIntent(SourcesIntent.UndoDelete)
                }
            }
        }
    }

    FluxScaffold(
        title = stringResource(Res.string.sources),
        onBackTap = { sendIntent(SourcesIntent.OnBackTap) },
        showBackButton = !content.fromSetup,
        floatingActionButton = {
            if (content.fromSetup) {
                ExtendedFloatingActionButton(
                    onClick = { sendIntent(SourcesIntent.OnNextTap) }
                ) {
                    Text.Button.Default(stringResource(Res.string.next))
                }
            }
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(hostState = it) } }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }

            item {
                Text.Annotated(
                    modifier = Modifier
                        .padding(bottom = FluxUI.Space.small)
                        .padding(horizontal = FluxUI.Space.medium.times(2))
                    ,
                    text = sourcesAnnotatedString(Res.string.system_folders_toggle_desc),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                ListItem(
                    modifier = Modifier
                        .padding(horizontal = FluxUI.Space.medium)
                        .clip(CircleShape)
                        .fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = { sendIntent(SourcesIntent.OnSystemFoldersSwitch)},
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Text.List.Title(text = stringResource(Res.string.system_folders))
                    },
                    trailingContent = {
                        Switch(
                            checked = content.systemFoldersEnabled,
                            onCheckedChange = { sendIntent(SourcesIntent.OnSystemFoldersSwitch) },
                        )
                    },
                )
            }

            item {
                AnimatedVisibility(
                    visible = content.systemFoldersEnabled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(all = FluxUI.Space.medium)
                            .clip(RoundedCornerShape(FluxUI.shapes.listItem)),
                        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.listItem)
                    ) {

                        SystemSourceItem(name = Environment.DIRECTORY_MOVIES)
                        SystemSourceItem(name = Environment.DIRECTORY_DOWNLOADS)
                    }
                }
            }

            item {
                TextButton(
                    modifier = Modifier
                        .padding(top = FluxUI.Space.medium)
                        .padding(horizontal = FluxUI.Space.medium),
                    onClick = { sendIntent(SourcesIntent.OpenFolderSelection) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
                    ) {
                        Icon(painter = painterResource(Res.drawable.ic_add), contentDescription = "")
                        Text.List.Section(text = stringResource(Res.string.add_source))
                    }

                }
            }

            val folders = content.folders.filterNot { it.path == content.waitingDeleteFolder?.path }
            itemsIndexed(
                items = folders,
                key = { _, folder -> folder.path },
            ) { index, folder ->

                CustomSourceItem(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxSize(),
                    folder = folder,
                    index = index,
                    lastIndex = folders.lastIndex,
                    onDelete = { sendIntent(SourcesIntent.Delete(folder)) }
                )

                if (index != folders.lastIndex)
                    Spacer(Modifier.height(FluxUI.Space.listItem))

            }

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 50.dp))
            }

        }

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