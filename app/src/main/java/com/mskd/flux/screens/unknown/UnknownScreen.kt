package com.mskd.flux.screens.unknown

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.identification.domain.model.IdentificationSource
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.identification.ui.IdentificationDialog
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.unknown.presentation.UnknownDialog
import com.mskd.flux.features.unknown.presentation.UnknownEvent
import com.mskd.flux.features.unknown.presentation.UnknownIntent
import com.mskd.flux.features.unknown.presentation.UnknownViewModel
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Route.Player
import com.mskd.flux.screens.unknown.composables.UnknownDropDownMenu
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.FluxSearchField
import com.mskd.flux.ui.components.LoadingScreen
import com.mskd.flux.ui.media.episode.EpisodeItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_help
import flux.shared.generated.resources.no_item
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.oups_an_error_occured
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UnknownScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: UnknownViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                UnknownEvent.BackToPreviousScreen -> onBack()
                UnknownEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
                is UnknownEvent.OpenInternalPlayer -> navigate(Player(params = PlayerParams.fromMedia(event.media)))
            }
        }
    }

    Crossfade(targetState = uiState.screen) { screen ->

        when (screen) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonClick = { viewModel.handleIntent(UnknownIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                UnknownScreenContent(
                    medias = uiState.filteredMedias,
                    searchQuery = uiState.searchQuery,
                    identification = uiState.identification,
                    dialog = uiState.dialog,
                    sendIntent = viewModel::handleIntent
                )
            }
        }

    }

}

@Composable
fun UnknownScreenContent(
    medias: List<Episode>,
    searchQuery: String,
    identification: IdentificationAvailability = IdentificationAvailability.HIDDEN,
    dialog: UnknownDialog? = null,
    sendIntent: (UnknownIntent) -> Unit
) {

    val focusManager = LocalFocusManager.current
    val layoutDirection = LocalLayoutDirection.current
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection) + FluxUI.Space.medium,
                    end = innerPadding.calculateEndPadding(layoutDirection) + FluxUI.Space.medium
                ),
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            FluxSearchField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = FluxUI.Space.large),
                value = searchQuery,
                onValueChange = { sendIntent(UnknownIntent.DoSearch(it)) },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + FluxUI.Space.large),
                state = lazyColumnState
            ) {

                if (medias.isEmpty()) {
                    item {

                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {

                            Text.Content.Body(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(),
                                text = stringResource(Res.string.no_item),
                                textAlign = TextAlign.Center
                            )

                        }

                    }
                }

                items(items = medias, key = { m -> m.id }) { media ->

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = media,
                        isSelected = false,
                        onClick = { sendIntent(UnknownIntent.PlayMedia(media = media)) },
                        dropDownMenu = { isVisible, onDismissRequest ->
                            UnknownDropDownMenu(
                                isVisible = isVisible,
                                episode = media,
                                identification = identification,
                                onDismissRequest = onDismissRequest,
                                sendIntent = sendIntent
                            )
                        }
                    )

                }

            }

        }

        (dialog as? UnknownDialog.Identification)?.let {
            IdentificationDialog(
                source = IdentificationSource.UnknownMedia(media = it.media),
                onDismiss = { sendIntent(UnknownIntent.CloseDialog) },
                onIdentified = { _, _ -> sendIntent(UnknownIntent.OnIdentified) },
            )
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