package com.kaem.flux.screens.player

import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.C
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.forceScreenOn
import com.kaem.flux.utils.hideSystemBars
import com.kaem.flux.utils.setAppInLandscape
import com.kaem.flux.utils.setAppOrientation
import com.kaem.flux.utils.showSystemBars

@Composable
fun PlayerScreen(
    artwork: Artwork?,
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
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(artwork.file.path)), artwork.currentTime)
                prepare()
                play()
        }
    }

    val audioDialog by lazy { TrackSelectionDialogBuilder(activity, "Audio", exoPlayer, C.TRACK_TYPE_AUDIO).build() }
    val subtitlesDialog by lazy { TrackSelectionDialogBuilder(activity, "Subtitles", exoPlayer, TRACK_TYPE_TEXT).build() }

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
        onTimeSave(exoPlayer.currentPosition)
        onBackButtonTap()
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
            }
        )

    }

    PlayerButtons(
        artwork = artwork,
        showButtons = showButtons,
        onBackButtonTap = {
            onTimeSave(exoPlayer.currentPosition)
            onBackButtonTap()
        },
        onAudioTap = { if (!audioDialog.isShowing) audioDialog.show() },
        onSubtitlesTap = { if (!subtitlesDialog.isShowing) subtitlesDialog.show() }
    )

}

@Composable
fun PlayerButtons(
    artwork: Artwork,
    showButtons: Boolean,
    onBackButtonTap: () -> Unit,
    onAudioTap: () -> Unit,
    onSubtitlesTap: () -> Unit
) {

    AnimatedVisibility(visible = showButtons) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            constraintSet = PlayerButtonsConstraintSet
        ) {

            PlayerBackButton(
                layoutId = "back",
                onTap = { onBackButtonTap() }
            )

            PlayerTitle(
                layoutId = "title",
                artwork = artwork
            )

            /*Column(
                modifier = Modifier.layoutId("buttons"),
                verticalArrangement = Arrangement.spacedBy(FluxSpace.EXTRA_SMALL),
                horizontalAlignment = Alignment.End
            ) {

                PlayerAudioButton(onTap = onAudioTap)

                PlayerSubtitlesButton(onTap = onSubtitlesTap)

            }*/

        }

    }

}

@Composable
fun PlayerBackButton(
    layoutId: String,
    onTap: () -> Unit
) {

    Box(
        modifier = Modifier
            .layoutId(layoutId)
            .padding(start = FluxSpace.MEDIUM)
            .size(50.dp)
            .clip(shape = CircleShape)
            .clickable { onTap() }
            .padding(FluxSpace.EXTRA_SMALL),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            tint = Color.White,
            contentDescription = "back button"
        )

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
        verticalArrangement = Arrangement.spacedBy(FluxSpace.EXTRA_SMALL)
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = artwork.title,
            color = Color.White,
            fontSize = FluxFontSize.MEDIUM,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )

        (artwork as? Episode)?.let { episode ->

            val season = stringResource(R.string.season, episode.season)
            val number = stringResource(R.string.episode, episode.number)

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$season, $number",
                color = Color.White,
                fontSize = FluxFontSize.SMALL,
                maxLines = 1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )
        }

    }

}
@Composable
fun PlayerAudioButton(
    onTap: () -> Unit
) {

    Box(
        modifier = Modifier
            .padding(start = FluxSpace.MEDIUM)
            .size(50.dp)
            .clip(shape = CircleShape)
            .clickable { onTap() }
            .padding(FluxSpace.EXTRA_SMALL),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painterResource(R.drawable.audio),
            tint = Color.White,
            contentDescription = "audio button"
        )

    }

}

@Composable
fun PlayerSubtitlesButton(
    onTap: () -> Unit
) {

    Box(
        modifier = Modifier
            .padding(start = FluxSpace.MEDIUM)
            .size(50.dp)
            .clip(shape = CircleShape)
            .clickable { onTap() }
            .padding(FluxSpace.EXTRA_SMALL),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painterResource(R.drawable.subtitles),
            tint = Color.White,
            contentDescription = "subtitles button"
        )

    }

}

val PlayerButtonsConstraintSet = ConstraintSet {

    val back = createRefFor("back")
    constrain(back) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    }

    val title = createRefFor("title")
    constrain(title) {
        top.linkTo(parent.top, FluxSpace.EXTRA_SMALL)
        start.linkTo(back.end, FluxSpace.SMALL)
        end.linkTo(parent.end, FluxSpace.MEDIUM)
        width = Dimension.fillToConstraints
    }

    val buttons = createRefFor("buttons")
    constrain(buttons) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        end.linkTo(parent.end, FluxSpace.SMALL)
    }

}