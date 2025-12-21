package com.kaem.flux.screens.player

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.composables.PlayerInterface
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars

@Composable
fun PlayerScreen(
    media: Media,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel<PlayerViewModel, PlayerViewModel.Factory>(
        creationCallback = { factory -> factory.create(media = media) }
    )
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as ComponentActivity
    val orientation = remember { activity.requestedOrientation }

    DisposableEffect(Unit) {
        activity.setAppInLandscape()
        activity.forceScreenOn(true)
        onDispose {
            activity.forceScreenOn(false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PlayerEvent.BackToPreviousScreen -> {
                    activity.setAppOrientation(orientation)
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(state.showInterface) {
        if (state.showInterface) activity.showSystemBars() else activity.hideSystemBars()
    }

    Crossfade(state.screen) { screen ->
        when (screen) {
            ScreenState.LOADING -> LoadingScreen()
            ScreenState.CONTENT -> {
                PlayerContent(
                    media = media,
                    exoPlayer = viewModel.player,
                    state = state,
                    sendIntent = viewModel::handleIntent
                )
            }
            ScreenState.ERROR -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(PlayerIntent.OnBackTap()) }
                )
            }
        }

    }

}

@OptIn(UnstableApi::class)
@Composable
fun PlayerContent(
    media: Media,
    exoPlayer: ExoPlayer,
    state: PlayerUiState,
    sendIntent: (PlayerIntent) -> Unit
) {

    val activity = LocalActivity.current as ComponentActivity

    val renderersFactory = DefaultRenderersFactory(activity)
    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

    LaunchedEffect(media) {
        exoPlayer.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
        exoPlayer.seekTo(media.currentTime)
        exoPlayer.prepare()
    }

    LifecycleComponent(
        onDispose = {
            activity.showSystemBars()
            exoPlayer.release()
        },
        onBackground = {
            exoPlayer.pause()
            sendIntent(PlayerIntent.SaveTime(time = exoPlayer.currentPosition))
        },
        onForeground = {
            if (!exoPlayer.isPlaying) exoPlayer.play()
        }
    )

    BackHandler(enabled = true) {
        sendIntent(PlayerIntent.OnBackTap(time = exoPlayer.currentPosition))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                sendIntent(PlayerIntent.ShowInterface(!state.showInterface))
            }
    ) {

        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = {
                if (state.showInterface)
                    it.showController()
            }
        )

    }

    PlayerInterface(
        media = media,
        state = state,
        exoPlayer = exoPlayer,
        sendIntent = sendIntent,
    )

}