package com.mskd.flux.screens.player.controllers

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
import com.mskd.flux.screens.player.PlayerEvent
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.ui.component.LifecycleComponent
import com.mskd.flux.utils.extensions.findActivity
import kotlinx.coroutines.launch

@Composable
fun PlayerSideEffects(
    viewModel: PlayerViewModel,
    stateHolder: PlayerStateHolder,
    windowStateHolder: WindowStateHolder,
    showInterface: Boolean,
    onBack: () -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation }
    var wasPlayingBeforeBackground by rememberSaveable { mutableStateOf(false) }

    // Set landscape and reset orientation on dispose
    DisposableEffect(Unit) {
        windowStateHolder.setLandscape()
        onDispose {
            windowStateHolder.updateSystemBars(true)
            originalOrientation?.let { windowStateHolder.resetOrientation(originalOrientation) }
        }
    }

    // Show system bars at the same time than interface
    LaunchedEffect(showInterface) {
        windowStateHolder.updateSystemBars(showInterface)
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
                stateHolder.event.collect { event ->
                    when (event) {
                        is PlayerStateHolder.Event.IsPlaying -> viewModel.handleIntent(PlayerIntent.SetPlayingStatus(isPlaying = event.isPlaying))
                        is PlayerStateHolder.Event.SelectedTrack -> viewModel.handleIntent(PlayerIntent.OnTrackSelected(event.track))
                        is PlayerStateHolder.Event.ShowNext -> viewModel.handleIntent(PlayerIntent.ShowNextEpisode(show = event.show))
                    }
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
                        PlayerEvent.SaveTimeRequested -> viewModel.handleIntent(PlayerIntent.SaveTime(time = stateHolder.player.currentPosition))
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