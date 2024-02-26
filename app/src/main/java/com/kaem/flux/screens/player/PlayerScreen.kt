package com.kaem.flux.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
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

@Composable
fun VideoPlayer(videoUri: Uri) {

    val localContext = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(localContext).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    
    val playbackState = exoPlayer.playbackState
    val isPlaying = exoPlayer.isPlaying

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    IconButton(
        onClick = {
            if (isPlaying) exoPlayer.pause()
            else exoPlayer.play()
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Warning else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )

    }
}