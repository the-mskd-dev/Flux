package com.kaem.flux.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kaem.flux.ui.theme.FluxSpace

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

   VideoPlayer(
       videoUri = Uri.parse(uiState.path),
       onBackButtonTap = onBackButtonTap
   )

}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUri: Uri,
    onBackButtonTap: () -> Unit,
) {

    val localContext = LocalContext.current
    var showBackButton by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(localContext).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            play()
        }
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    showController()
                    setShowPreviousButton(false)
                    setShowNextButton(false)
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                        showBackButton = it == View.VISIBLE
                    })
                }
            }
        )

        AnimatedVisibility(visible = showBackButton) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = FluxSpace.MEDIUM)
                    .size(50.dp)
                    .clip(shape = CircleShape)
                    .clickable { onBackButtonTap() }
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
                    imageVector = Icons.Rounded.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "back button"
                )

            }
        }

    }

}