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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Artwork
import com.mskd.flux.screens.artwork.ArtworkContent
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.show.composables.SeasonDialog
import com.mskd.flux.screens.show.composables.SeasonItem
import com.mskd.flux.screens.show.composables.ShowContentLarge
import com.mskd.flux.screens.show.composables.ShowContentRegular
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDropDownMenu
import com.mskd.flux.ui.component.FluxDropDownMenuItem
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.OverviewItem
import com.mskd.flux.ui.component.ResetProgressDialog
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
        targetState = uiState.state::class,
        label = "MediaScreenAnimation"
    ) { state ->

        when (state) {
            State.Loading::class -> LoadingScreen()
            State.Error::class -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ShowIntent.OnBackTap) }
                )
            }
            State.Content::class -> {
                val content = (uiState.state as State.Content<ShowContent>).content
                MaterialTheme(colorScheme = colorScheme) {
                    ShowScreenContent(
                        fullShow = content.fullShow,
                        dialog = content.dialog,
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

    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isLargeScreen = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

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

        if (isLargeScreen) {
            ShowContentLarge(
                fullShow = fullShow,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        } else {
            ShowContentRegular(
                fullShow = fullShow,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        }

        (dialog as? ShowDialog.SeasonPreview)?.let {
            SeasonDialog(
                season = it.season,
                sendIntent = sendIntent,
            )
        }

        if (dialog is ShowDialog.ResetProgress) {
            ResetProgressDialog(
                onValidate = { sendIntent(ShowIntent.ResetProgress) },
                onDismiss = { sendIntent(ShowIntent.CloseDialog) }
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