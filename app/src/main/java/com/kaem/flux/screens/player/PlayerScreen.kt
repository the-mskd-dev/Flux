package com.kaem.flux.screens.player

import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.SmallText
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars
import java.util.Locale

@Composable
fun PlayerScreen(
    artwork: Artwork?,
    backward: Long,
    forward: Long,
    subtitlesLanguage: Locale,
    onBackButtonTap: () -> Unit,
    onTimeSave: (Long) -> Unit
) {

    var isExiting by remember { mutableStateOf(false) }
    val activity = LocalContext.current as ComponentActivity
    val orientation = remember { activity.requestedOrientation }

    DisposableEffect(Unit) {
        activity.setAppInLandscape()
        activity.forceScreenOn(true)
        onDispose {
            activity.forceScreenOn(false)
        }
    }

    if (!isExiting) {
        if (artwork != null) {
            VideoPlayer(
                artwork = artwork,
                backward = backward,
                forward = forward,
                subtitlesLanguage = subtitlesLanguage,
                onBackButtonTap = {
                    activity.setAppOrientation(orientation)
                    isExiting = true
                    onBackButtonTap()
                },
                onTimeSave = onTimeSave
            )
        } else {
            Box(modifier = Modifier.background(Color.Black).fillMaxSize())
        }
    }

}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    artwork: Artwork,
    backward: Long,
    forward: Long,
    subtitlesLanguage: Locale,
    onBackButtonTap: () -> Unit,
    onTimeSave: (Long) -> Unit
) {

    val activity = LocalContext.current as ComponentActivity
    var showButtons by remember { mutableStateOf(false) }

    val renderersFactory = DefaultRenderersFactory(activity)
    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

    val exoPlayer = remember {
        ExoPlayer.Builder(activity)
            .setRenderersFactory(renderersFactory)
            .setSeekBackIncrementMs(backward)
            .setSeekForwardIncrementMs(forward)
            .build()
            .apply {
                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    .setPreferredTextLanguage(subtitlesLanguage.language)
                    .setPreferredTextRoleFlags(C.ROLE_FLAG_SUBTITLE)
                    .build()
                setMediaItem(MediaItem.fromUri(Uri.parse(artwork.file.path)), artwork.currentTime)
                prepare()
                play()
        }
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            activity.showSystemBars()
            exoPlayer.release()
        }
    }

    LaunchedEffect(showButtons) {
        if (showButtons) activity.showSystemBars() else activity.hideSystemBars()
    }

    LifecycleComponent(
        onBackground = {
            exoPlayer.pause()
            onTimeSave(exoPlayer.currentPosition)
        },
        onForeground = {
            if (!exoPlayer.isPlaying) exoPlayer.play()
        }
    )

    BackHandler(enabled = true) {
        if (showButtons) {
            onTimeSave(exoPlayer.currentPosition)
            onBackButtonTap()
        } else {
            showButtons = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    showController()
                    setShowSubtitleButton(true)
                    setShowPreviousButton(false)
                    setShowNextButton(false)
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                        showButtons = it == View.VISIBLE
                    })
                }
            },
            update = {
                if (showButtons)
                    it.showController()
            }
        )

    }

    PlayerButtons(
        artwork = artwork,
        showButtons = showButtons,
        onBackButtonTap = {
            onTimeSave(exoPlayer.currentPosition)
            onBackButtonTap()
        }
    )

}

@Composable
fun PlayerButtons(
    artwork: Artwork,
    showButtons: Boolean,
    onBackButtonTap: () -> Unit
) {

    AnimatedVisibility(visible = showButtons) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            constraintSet = PlayerButtonsConstraintSet
        ) {

            BackButton(
                modifier = Modifier.layoutId("back"),
                tint = Color.White,
                onTap = onBackButtonTap
            )

            PlayerTitle(
                layoutId = "title",
                artwork = artwork
            )

        }

    }

}

@Composable
fun PlayerTitle(
    layoutId: String,
    artwork: Artwork
) {

    Column(
        modifier = Modifier.layoutId(layoutId),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
    ) {

        MediumText(
            modifier = Modifier.fillMaxWidth(),
            text = artwork.title,
            color = Color.White,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )

        (artwork as? Episode)?.let { episode ->

            val season = stringResource(R.string.season, episode.season)
            val number = stringResource(R.string.episode, episode.number)

            SmallText(
                modifier = Modifier.fillMaxWidth(),
                text = "$season, $number",
                color = Color.White,
                maxLines = 1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )

        }

    }

}

val PlayerButtonsConstraintSet = ConstraintSet {

    val back = createRefFor("back")
    constrain(back) {
        top.linkTo(parent.top)
        start.linkTo(parent.start, Ui.Space.MEDIUM)
    }

    val title = createRefFor("title")
    constrain(title) {
        top.linkTo(parent.top, Ui.Space.EXTRA_SMALL)
        start.linkTo(back.end, Ui.Space.SMALL)
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints
    }

}