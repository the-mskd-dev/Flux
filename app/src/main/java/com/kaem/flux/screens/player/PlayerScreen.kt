package com.kaem.flux.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun PlayerScreen(
    onBackButtonTap: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    DisposableEffect(orientation) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }

   VideoPlayer(videoUri = Uri.parse(uiState.path))

}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(videoUri: Uri) {

    val localContext = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(localContext).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            play()
        }
    }
    
    val playbackState = exoPlayer.playbackState
    val isPlaying = exoPlayer.isPlaying

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                showController()
                setShowPreviousButton(false)
                setShowNextButton(false)
            }
        },
        modifier = Modifier.fillMaxSize(),
    )

}