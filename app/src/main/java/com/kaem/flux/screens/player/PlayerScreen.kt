package com.kaem.flux.screens.player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.media3.common.C
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.C.TrackType
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.player.Metadata
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.theme.FluxSpace

@Composable
fun PlayerScreen(
    artwork: Artwork?,
    onBackButtonTap: () -> Unit,
    onTimeSave: (Long) -> Unit
) {

    var isExiting by remember { mutableStateOf(false) }
    val activity = LocalContext.current as ComponentActivity
    val exitScreen = remember {
        { orientation: Int ->
            if (activity.requestedOrientation != orientation) {
                activity.requestedOrientation = orientation
            }
            isExiting = true
            onBackButtonTap()
        }
    }

    DisposableEffect(Unit) {
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }

    if (!isExiting) {
        if (artwork != null) {
            VideoPlayer(
                path = artwork.file.path,
                currentTime = artwork.currentTime,
                onBackButtonTap = {
                    exitScreen(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
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
    path: String,
    currentTime: Long,
    onBackButtonTap: () -> Unit,
    onTimeSave: (Long) -> Unit,
) {

    val localContext = LocalContext.current
    var showButtons by remember { mutableStateOf(false) }


    val exoPlayer = remember {
        ExoPlayer.Builder(localContext)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(path)), currentTime)
                prepare()
                play()
        }
    }

    val audioDialog = TrackSelectionDialogBuilder(localContext, "Audio", exoPlayer, C.TRACK_TYPE_AUDIO).build()
    val subtitlesDialog = TrackSelectionDialogBuilder(localContext, "Subtitles", exoPlayer, TRACK_TYPE_TEXT).build()

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
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
                    setShowSubtitleButton(false)
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

            Button(
                modifier = Modifier.layoutId("audio"),
                onClick = onAudioTap
            ) {
                Text(
                    text = "Audio",
                    color = Color.White
                )
            }

            Button(
                modifier = Modifier.layoutId("subtitles"),
                onClick = onSubtitlesTap
            ) {
                Text(
                    text = "Subtitles",
                    color = Color.White
                )
            }

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
            .padding(start = FluxSpace.MEDIUM)
            .layoutId(layoutId)
            .size(50.dp)
            .clip(shape = CircleShape)
            .clickable { onTap() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = CircleShape
            )
            .background(color = MaterialTheme.colorScheme.background)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = "back button"
        )

    }

}

val PlayerButtonsConstraintSet = ConstraintSet {

    val back = createRefFor("back")
    constrain(back) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    }

    val audio = createRefFor("audio")
    constrain(audio) {
        top.linkTo(back.bottom, 8.dp)
        start.linkTo(parent.start)
    }

    val subtitles = createRefFor("subtitles")
    constrain(subtitles) {
        top.linkTo(audio.bottom, 4.dp)
        start.linkTo(parent.start)
    }

}