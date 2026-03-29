package com.kaem.flux.screens.welcome

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.lerp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.navigation.Route
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxIconButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WelcomeScreen(
    navigate: (Route) -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = fluxPermissionState()
    val pagerState = rememberPagerState(pageCount = { WelcomePage.entries.size })

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                WelcomeEvent.NavigateToLibrary -> navigate(Route.Library)
                WelcomeEvent.NavigateToToken -> navigate(Route.Token(fromSettings = false))
                WelcomeEvent.OpenPermissionDialog -> permissions.launchPermissionRequest()
                is WelcomeEvent.ScrollToPage -> {
                    pagerState.animateScrollToPage(event.pageIndex)
                }
            }
        }
    }

    if (permissions.status.isGranted) {
        viewModel.handleIntent(WelcomeIntent.OnPermissionGranted)
    }

    BackHandler(enabled = uiState.pageIndex > 0) {
        viewModel.handleIntent(WelcomeIntent.OnPreviousTap)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.targetPage }.collect { pageIndex ->
            viewModel.handleIntent(WelcomeIntent.OnPageChange(pageIndex))
        }
    }

    WelcomeScreenContent(
        uiState = uiState,
        pagerState = pagerState,
        sendIntent = viewModel::handleIntent,
    )

}

@Composable
fun WelcomeScreenContent(
    uiState: WelcomeUiState,
    pagerState: PagerState,
    sendIntent: (WelcomeIntent) -> Unit
) {

    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
        constraintSet = WelcomeScreenConstraintSet
    ) {

        WelcomeBackground(
            modifier = Modifier.layoutId("background"),
            drawableId = WelcomePage.entries[uiState.pageIndex].drawableId
        )

        WelcomePager(
            modifier = Modifier.layoutId("pager"),
            pagerState = pagerState
        )

        WelcomeButtons(
            modifier = Modifier
                .layoutId("buttons")
                .navigationBarsPadding(),
            buttons = uiState.buttons,
            sendIntent = sendIntent
        )

    }

}

@Composable
fun WelcomePager(
    modifier: Modifier,
    pagerState: PagerState,
) {

    HorizontalPager(
        modifier = modifier,
        state = pagerState
    ) { index ->

        val page = WelcomePage.entries[index]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Ui.Space.MEDIUM)
                .graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - index) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        start = 0.2f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                Text.Headline.Large(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(page.titleId),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text.Body.Large(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(page.descriptionId),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

        }


    }

}

@Composable
fun WelcomeBackground(
    modifier: Modifier,
    drawableId: Int
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = drawableId,
            transitionSpec = { (fadeIn()  + scaleIn(initialScale = 0.92f)) togetherWith fadeOut() },
            label = "background animation"
        ) { id ->

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (Random.nextBoolean()) {
                            translationY = -15f
                            translationX = -15f
                            rotationZ = 30f
                            rotationX = 30f
                        } else {
                            translationY = -15f
                            translationX = 15f
                            rotationZ = -15f
                            rotationX = 15f
                        }
                        cameraDistance = 15f
                    },
                painter = painterResource(id),
                contentDescription = "app presentation image"
            )

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeButtons(
    modifier: Modifier,
    buttons: List<WelcomeButton>,
    sendIntent: (WelcomeIntent) -> Unit
) {

    Box(
        modifier = modifier,
    ) {

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterStart),
            visible = buttons.contains(WelcomeButton.PREVIOUS),
            enter = Ui.Animation.buttonEnter,
            exit = Ui.Animation.buttonExit,
        ) {

            FluxIconButton(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                onTap = { sendIntent(WelcomeIntent.OnPreviousTap) },
                contentDescription = "previous button"
            )

        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = buttons.contains(WelcomeButton.PERMISSIONS),
            enter = Ui.Animation.buttonEnter,
            exit = Ui.Animation.buttonExit,
        ) {

            FluxButton(
                onTap = { sendIntent(WelcomeIntent.OnPermissionTap) },
                text = stringResource(id = R.string.give_permission),
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary,
            )

        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = buttons.contains(WelcomeButton.NEXT),
            enter = Ui.Animation.buttonEnter,
            exit = Ui.Animation.buttonExit,
        ) {

            FluxIconButton(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onTap = { sendIntent(WelcomeIntent.OnNextTap) },
                contentDescription = "next button"
            )

        }


    }

}

val WelcomeScreenConstraintSet = ConstraintSet {

    val (background, pager, buttons) = createRefsFor(
        "background",
        "pager",
        "buttons"
    )

    constrain(background) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
    }

    constrain(pager) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
    }

    constrain(buttons) {
        bottom.linkTo(parent.bottom, Ui.Space.MEDIUM)
        start.linkTo(parent.start, Ui.Space.MEDIUM)
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints
    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun fluxPermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}

@FluxPreview
@Composable
fun WelcomeScreen_Preview() {
    AppTheme {
        WelcomeScreenContent(
            uiState = WelcomeUiState(
                buttons = WelcomeButton.entries
            ),
            pagerState = rememberPagerState(pageCount = { WelcomePage.entries.size }),
            sendIntent = {}
        )
    }
}