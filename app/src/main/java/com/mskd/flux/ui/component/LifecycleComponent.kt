package com.mskd.flux.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mskd.flux.utils.Trace

@Composable
fun LifecycleComponent(
    onDispose: () -> Unit = {},
    onBackground: () -> Unit = {},
    onForeground: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect (lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Trace.info("Lifecycle", "App pushed to the foreground")
                    onForeground()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Trace.info("Lifecycle", "App pushed to the background")
                    onBackground()
                }
                Lifecycle.Event.ON_STOP -> {
                    Trace.info("Lifecycle", "App stopped")
                    onStop()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onDispose()
        }
    }
}