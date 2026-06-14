package com.mskd.flux.ui.component

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

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
                    Log.i("Lifecycle", "App pushed to the foreground")
                    onForeground()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.i("Lifecycle", "App pushed to the background")
                    onBackground()
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.i("Lifecycle", "App stopped")
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