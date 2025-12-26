package com.kaem.flux.screens.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.utils.extensions.findActivity
import kotlinx.coroutines.launch

@Composable
fun PlayerSideEffects(
    viewModel: PlayerViewModel,
    stateHolder: PlayerStateHolder,
    showInterface: Boolean,
    onBack: () -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation }
    var wasPlayingBeforeBackground by rememberSaveable { mutableStateOf(false) }

    // Set landscape and reset orientation on dispose
    DisposableEffect(Unit) {
        stateHolder.setLandscape()
        onDispose {
            stateHolder.updateSystemBars(true)
            originalOrientation?.let { stateHolder.resetOrientation(originalOrientation) }
        }
    }

    // Show system bars at the same time than interface
    LaunchedEffect(showInterface) {
        stateHolder.updateSystemBars(showInterface)
    }

    // Observe tracks
    val tracks by stateHolder.tracks.collectAsStateWithLifecycle()
    LaunchedEffect(tracks) {
        viewModel.handleIntent(PlayerIntent.UpdateTracks(tracks))
    }

    // Observe events
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

            // Events from SH
            launch {
                stateHolder.selectedTrack.collect { track ->
                    viewModel.handleIntent(PlayerIntent.OnTrackSelected(track))
                }
            }

            launch {
                stateHolder.isPlaying.collect { isPlaying ->
                    viewModel.handleIntent(PlayerIntent.SetPlayingStatus(isPlaying = isPlaying))
                }
            }

            launch {
                stateHolder.showNext.collect { showNext ->
                    viewModel.handleIntent(PlayerIntent.ShowNextEpisode(show = showNext))
                }
            }

            // Events from VM
            launch {
                viewModel.event.collect { event ->
                    when (event) {
                        PlayerEvent.BackToPreviousScreen -> onBack()
                        is PlayerEvent.SeekRewind -> stateHolder.onFastRewind(event.time)
                        is PlayerEvent.SeekForward -> stateHolder.onFastForward(event.time)
                        is PlayerEvent.UpdateProgress -> stateHolder.updateProgress(event.progress)
                        is PlayerEvent.SelectTrack -> stateHolder.selectTrack(event.track)
                        PlayerEvent.TogglePlayButton -> stateHolder.togglePlayButton()
                    }
                }
            }
        }
    }

    LifecycleComponent(
        onBackground = {
            wasPlayingBeforeBackground = stateHolder.player.isPlaying
            if (wasPlayingBeforeBackground) {
                viewModel.handleIntent(PlayerIntent.TogglePlayButton)
            }

            viewModel.handleIntent(PlayerIntent.SaveTime(time = stateHolder.player.currentPosition))
        },
        onForeground = {
            if (wasPlayingBeforeBackground) {
                viewModel.handleIntent(PlayerIntent.TogglePlayButton)
            }
        }
    )

}