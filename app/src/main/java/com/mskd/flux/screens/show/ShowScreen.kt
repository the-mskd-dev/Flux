package com.mskd.flux.screens.show

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.*
import com.mskd.flux.screens.artwork.ArtworkDropDownMenu
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.show.composables.SeasonDialog
import com.mskd.flux.screens.show.composables.SeasonItem
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDropDownMenu
import com.mskd.flux.ui.component.FluxDropDownMenuItem
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.OverviewItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.WebLink

@Composable
fun ShowScreen(
    artworkId: Long,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowViewModel = hiltViewModel<ShowViewModel, ShowViewModel.Factory>(
        creationCallback = { factory -> factory.create(artworkId) }
    )
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ShowEvent.BackToPreviousScreen -> onBack()
                is ShowEvent.NavigateToSeason -> navigate(Artwork(artworkId = event.artworkId, season = event.season, rgb = event.rgb))
                is ShowEvent.OpenShowInfo -> WebLink.openPage(context = context, url = event.url)
            }
        }
    }

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState.state,
        label = "MediaScreenAnimation"
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            State.Error -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ShowIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                MaterialTheme(colorScheme = colorScheme) {
                    ShowScreenContent(
                        fullShow = state.content as FullArtwork.FullShow,
                        dialog = uiState.dialog,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }

        }

    }

}

@Composable
fun ShowScreenContent(
    fullShow: FullArtwork.FullShow,
    dialog: ShowDialog?,
    sendIntent: (ShowIntent) -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val titleAlpha by remember {
        derivedStateOf {
            if (scrollBehavior.state.contentOffset < -10f) 1f else 0f
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = titleAlpha,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "TitleAlphaAnimation"
    )

    val columns = 3

    FluxScaffold(
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
        title = fullShow.artwork.title,
        onBackTap = { sendIntent(ShowIntent.OnBackTap) },
        scrollBehavior = scrollBehavior,
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        actions = {

            IconButton(
                onClick = { showMenu = true },
                content = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "menu button"
                    )
                }
            )

            if (showMenu) {
                ShowDropDownMenu(
                    onDismissRequest = { showMenu = false },
                    sendIntent = sendIntent
                )
            }

        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {

                Column(
                    verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
                ) {

                    ArtworkImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(Ui.Images.RATIO_6_5),
                        fullArtwork = fullShow,
                    )

                    OverviewItem(
                        modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM),
                        title = stringResource(R.string.summary),
                        description = fullShow.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                    )

                    Text.Title.Large(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Ui.Space.MEDIUM),
                        text = stringResource(R.string.seasons),
                        emphasized = true,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                }

            }

            item {
                Spacer(modifier = Modifier.height(Ui.Space.MEDIUM))
            }

            val seasonsChunks = fullShow.seasons.chunked(columns)

            items(
                items = seasonsChunks,
                key = { seasons -> seasons.fold("") { acc, s -> acc + s.id } }
            ) { seasons ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.MEDIUM)
                        .padding(bottom = Ui.Space.MEDIUM),
                    horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
                ) {

                    seasons.forEach { season ->

                        SeasonItem(
                            modifier = Modifier.weight(1f),
                            season = season,
                            episodes = fullShow.episodes.filter { it.season == season.season },
                            onTap = { sendIntent(ShowIntent.OnSeasonTap(season = season.season, rgb = it))},
                            onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
                        )

                    }

                    val emptySlots = columns - seasons.size
                    if (emptySlots > 0) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                }


            }

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }

        }

        (dialog as? ShowDialog.SeasonPreview)?.let {
            SeasonDialog(
                season = it.season,
                sendIntent = sendIntent,
            )
        }

    }

}

@Composable
fun ShowDropDownMenu(
    onDismissRequest: () -> Unit,
    sendIntent: (ShowIntent) -> Unit
) {

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = listOf(
            FluxDropDownMenuItem(
                text = stringResource(R.string.more_info),
                onClick = {
                    sendIntent(ShowIntent.OpenShowInfo)
                    onDismissRequest()
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.more_info)) },
            ),
            FluxDropDownMenuItem(
                text = stringResource(R.string.reset_progress),
                onClick = {
                    sendIntent(ShowIntent.ShowResetProgressDialog)
                    onDismissRequest()
                },
                leadingIcon = { Icon(painter = painterResource(R.drawable.ic_eraser), contentDescription = stringResource(R.string.reset_progress)) },
            )
        )
    )

}

@FluxPreview
@Composable
fun ShowScreenContent_Preview() {
    AppThemePreview {
        ShowScreenContent(
            fullShow = MediaMockups.fullShow,
            dialog = null,
            sendIntent = {}
        )
    }
}