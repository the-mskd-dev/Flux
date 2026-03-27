package com.kaem.flux.screens.welcome

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.kaem.flux.screens.token.TokenIntent
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxIconButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WelcomeScreen(
    navigate: (Route) -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = fluxPermissionState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                WelcomeEvent.NavigateToLibrary -> navigate(Route.Library)
                WelcomeEvent.NavigateToToken -> navigate(Route.Token(fromSettings = false))
                WelcomeEvent.OpenPermissionDialog -> permissions.launchPermissionRequest()
            }
        }
    }

    if (permissions.status.isGranted) {
        viewModel.handleIntent(WelcomeIntent.OnPermissionGranted)
    }

    BackHandler(enabled = uiState.index > 0) {
        viewModel.handleIntent(WelcomeIntent.OnPreviousTap)
    }

    WelcomeScreenContent(
        uiState = uiState,
        sendIntent = viewModel::handleIntent,
    )

}

@Composable
fun WelcomeScreenContent(
    uiState: WelcomeUiState,
    sendIntent: (WelcomeIntent) -> Unit
) {

    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
        constraintSet = WelcomeScreenConstraintSet
    ) {

        WelcomeBackground(
            modifier = Modifier.layoutId("background"),
            drawableId = uiState.backgroundId
        )

        WelcomePage(
            modifier = Modifier.layoutId("content"),
            contentIds = uiState.contentIds
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
            label = "background animation"
        ) { id ->

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -15f
                        translationX = 15f
                        rotationZ = -15f
                        rotationX = 15f
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

@Composable
fun WelcomePage(
    modifier: Modifier,
    contentIds: Pair<Int, Int>,
) {

    AnimatedContent(
        modifier = modifier,
        targetState = contentIds
    ) { content ->

        val (title, description) = content

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(horizontal = Ui.Space.MEDIUM),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                Text.Headline.Large(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(title),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text.Body.Large(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(description),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

        }

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
            enter = fadeIn(),
            exit = fadeOut()
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
            enter = fadeIn(),
            exit = fadeOut()
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
            enter = fadeIn(),
            exit = fadeOut()
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

    val (background, content, buttons) = createRefsFor(
        "background",
        "content",
        "buttons"
    )

    val guideline = createGuidelineFromTop(.7f)

    constrain(background) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
    }

    constrain(content) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(guideline)
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
    }

    constrain(buttons) {
        //top.linkTo(guideline)
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
            sendIntent = {}
        )
    }
}