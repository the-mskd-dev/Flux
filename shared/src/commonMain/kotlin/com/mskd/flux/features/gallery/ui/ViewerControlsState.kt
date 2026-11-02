package com.mskd.flux.features.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val AUTO_HIDE_DELAY = 3.seconds

/**
 * Visibility of the Gallery viewer controls: hidden on open, toggled by a single tap on the image,
 * hidden again after 3 s without interaction, except while an Image download runs.
 */
class ViewerControlsState(private val scope: CoroutineScope) {

    var visible by mutableStateOf(false)
        private set

    private var downloadRunning = false
    private var autoHide: Job? = null

    fun onTap() {
        visible = !visible
        restartAutoHide()
    }

    fun onButtonInteraction() {
        restartAutoHide()
    }

    fun onDownloadRunningChange(running: Boolean) {
        if (downloadRunning == running) return
        downloadRunning = running
        restartAutoHide()
    }

    private fun restartAutoHide() {
        autoHide?.cancel()
        if (!visible || downloadRunning) return
        autoHide = scope.launch {
            delay(AUTO_HIDE_DELAY)
            visible = false
        }
    }

}

@Composable
fun rememberViewerControlsState(): ViewerControlsState {
    val scope = rememberCoroutineScope()
    return remember(scope) { ViewerControlsState(scope) }
}
