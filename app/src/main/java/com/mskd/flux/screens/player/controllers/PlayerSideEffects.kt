package com.mskd.flux.screens.player.controllers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mskd.flux.screens.player.PlayerEvent
import com.mskd.flux.screens.player.PlayerIntent
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

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation }

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
        onBackground = { viewModel.handleIntent(PlayerIntent.GoToBackground) },
        onForeground = { viewModel.handleIntent(PlayerIntent.GoToForeground) }
    )

}