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
import com.mskd.flux.screens.player.PlayerIntent.SaveTime
import com.mskd.flux.screens.player.PlayerIntent.TogglePlayButton
import com.mskd.flux.screens.player.PlayerIntent.UpdateAmbientOverlay
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.ui.component.LifecycleComponent
import com.mskd.flux.utils.extensions.findActivity
import kotlinx.coroutines.launch

@Composable
fun PlayerSideEffects(
    viewModel: PlayerViewModel,
    windowStateHolder: WindowStateHolder,
    showInterface: Boolean,
    onBack: () -> Unit
) {

    val player by viewModel.playerManager.player.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation }
    var wasPlayingBeforeBackground by rememberSaveable { mutableStateOf(false) }

    // Set force screen on and reset orientation on dispose
    DisposableEffect(Unit) {
        windowStateHolder.forceScreenOn()
        onDispose {
            windowStateHolder.updateSystemBars(true)
            windowStateHolder.resetBrightness()
            originalOrientation?.let { windowStateHolder.resetOrientation(originalOrientation) }
        }
    }

    // Show system bars at the same time as interface
    LaunchedEffect(showInterface) {
        windowStateHolder.updateSystemBars(showInterface)
    }


    // Observe events
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

            // Events from VM
            launch {
                viewModel.event.collect { event ->
                    when (event) {
                        PlayerEvent.BackToPreviousScreen -> onBack()
                        PlayerEvent.SaveTimeRequested -> player?.let { viewModel.handleIntent(SaveTime(time = it.currentPosition)) }
                        is PlayerEvent.ChangeBrightness -> {
                            windowStateHolder.changeBrightness(delta = event.delta)?.let { brightness ->
                                viewModel.handleIntent(UpdateAmbientOverlay(type = PlayerUiState.AmbientOverlay.Type.BRIGHTNESS, value = brightness))
                            }
                        }
                    }
                }
            }
        }
    }

    LifecycleComponent(
        onBackground = {
            wasPlayingBeforeBackground = player?.isPlaying ?: false
            if (wasPlayingBeforeBackground) {
                viewModel.handleIntent(TogglePlayButton)
            }

            player?.let { viewModel.handleIntent(SaveTime(time = it.currentPosition)) }
        },
        onForeground = {
            if (wasPlayingBeforeBackground) {
                viewModel.handleIntent(TogglePlayButton)
            }
        }
    )

}