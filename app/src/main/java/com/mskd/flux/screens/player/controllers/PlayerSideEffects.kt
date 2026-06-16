package com.mskd.flux.screens.player.controllers

import android.app.PictureInPictureParams
import android.util.Rational
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mskd.flux.MainActivity
import com.mskd.flux.screens.player.PlayerEvent
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.screens.player.PlayerIntent.UpdateAmbientOverlay
import com.mskd.flux.screens.player.PlayerUiContent
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.ui.component.LifecycleComponent
import com.mskd.flux.utils.extensions.findActivity
import kotlinx.coroutines.launch

@Composable
fun PlayerSideEffects(
    viewModel: PlayerViewModel,
    windowStateHolder: WindowStateHolder,
    showInterface: Boolean,
    onBack: () -> Unit,
    isPlayingContent: () -> Boolean,
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity() as? MainActivity
    val originalOrientation = remember { activity?.requestedOrientation }
    val currentIsPlayingContent by rememberUpdatedState(isPlayingContent)

    // Observer PiP state
    val isInPip by produceState(initialValue = false, activity) {
        if (activity == null) return@produceState
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            value = info.isInPictureInPictureMode
        }
        activity.addOnPictureInPictureModeChangedListener(observer)
        awaitDispose {
            activity.removeOnPictureInPictureModeChangedListener(observer)
        }
    }

    DisposableEffect(activity) {
        activity?.setOnUserLeaveHintCallback {
            if (currentIsPlayingContent()) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                activity.enterPictureInPictureMode(params)
            }
        }
        onDispose {
            activity?.setOnUserLeaveHintCallback(null)
        }
    }

    LaunchedEffect(isInPip) { viewModel.handleIntent(PlayerIntent.OnPipChange(isInPip)) }

    // Set force screen on and reset orientation on dispose
    DisposableEffect(Unit) {
        windowStateHolder.forceScreenOn()
        onDispose {
            if (!isInPip) windowStateHolder.updateSystemBars(true)
            windowStateHolder.resetBrightness()
            originalOrientation?.let { windowStateHolder.resetOrientation(originalOrientation) }
        }
    }

    // Show system bars at the same time as interface
    LaunchedEffect(showInterface) {
        if (!isInPip) windowStateHolder.updateSystemBars(showInterface)
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
                                viewModel.handleIntent(UpdateAmbientOverlay(type = PlayerUiContent.AmbientOverlay.Type.BRIGHTNESS, value = brightness))
                            }
                        }
                    }
                }
            }
        }
    }

    LifecycleComponent(
        onBackground = { viewModel.handleIntent(PlayerIntent.GoToBackground) },
        onForeground = { viewModel.handleIntent(PlayerIntent.GoToForeground) },
        onStop = { if (isInPip) viewModel.handleIntent(PlayerIntent.OnClosePiP) }
    )

}