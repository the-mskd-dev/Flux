package com.mskd.flux.screens.unknown

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.unknown.presentation.UnknownEvent
import com.mskd.flux.features.unknown.presentation.UnknownIntent
import com.mskd.flux.features.unknown.presentation.UnknownViewModel
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.FluxSearchField
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.EpisodeItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.rememberExternalPlayerLauncher
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_help
import flux.shared.generated.resources.no_item
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.oups_an_error_occured
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UnknownScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: UnknownViewModel = koinViewModel()
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val externalPlayerLauncher = rememberExternalPlayerLauncher(
        context = context,
        onProgressResult = { progress ->
            viewModel.handleIntent(UnknownIntent.OnExternalPlayerResult(progress = progress))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                UnknownEvent.BackToPreviousScreen -> onBack()
                UnknownEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
                is UnknownEvent.PlayMedia -> navigate(Player(mediaId = event.mediaId))
                is UnknownEvent.LaunchExternalPlayer -> {
                    ExternalPlayer.launchPlayer(
                        context = context,
                        media = event.media,
                        launcher = externalPlayerLauncher,
                        onError = { viewModel.handleIntent(UnknownIntent.PlayMedia(media = event.media, forceInternal = true)) }
                    )
                }
            }
        }
    }

    Crossfade(targetState = uiState.screen) { screen ->

        when (screen) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(UnknownIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                UnknownScreenContent(
                    medias = uiState.filteredMedias,
                    searchQuery = uiState.searchQuery,
                    sendIntent = viewModel::handleIntent
                )
            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnknownScreenContent(
    medias: List<Episode>,
    searchQuery: String,
    sendIntent: (UnknownIntent) -> Unit
) {

    val focusManager = LocalFocusManager.current
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(lazyColumnState) {
        snapshotFlow { lazyColumnState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    focusManager.clearFocus()
                }
            }
    }

    FluxScaffold(
        title = stringResource(Res.string.other_files),
        onBackTap = { sendIntent(UnknownIntent.OnBackTap) },
        actions = {
            IconButton(onClick = { sendIntent(UnknownIntent.OnInfoTap) }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_help),
                    contentDescription = "Help icon button"
                )
            }
        }
    ) { innerPadding ->

        if (medias.isNotEmpty()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                state = lazyColumnState
            ) {

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                }

                item {

                    FluxSearchField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FluxUI.Space.medium)
                            .padding(bottom = FluxUI.Space.large),
                        value = searchQuery,
                        onValueChange = { sendIntent(UnknownIntent.DoSearch(it)) },
                    )

                }

                items(items = medias, key = { m -> m.id }) { media ->

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = media,
                        isSelected = false,
                        onTap = { sendIntent(UnknownIntent.PlayMedia(media = media)) },
                    )

                }

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
                }

            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding(),
                contentAlignment = Alignment.TopStart
            ) {

                Text.Body.Large(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = stringResource(Res.string.no_item),
                    textAlign = TextAlign.Center
                )

            }

        }

    }

}

@FluxPreview
@Composable
fun UnknownScreen_Preview() {
    FluxTheme {
        UnknownScreenContent(
            medias = MediaMockups.episodesWithStatus,
            searchQuery = "",
            sendIntent = {}
        )
    }
}