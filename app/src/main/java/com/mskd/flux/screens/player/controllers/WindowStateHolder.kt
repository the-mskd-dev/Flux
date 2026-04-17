package com.mskd.flux.screens.player.controllers

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mskd.flux.utils.extensions.findActivity
import com.mskd.flux.utils.extensions.forceScreenOn
import com.mskd.flux.utils.extensions.hideSystemBars
import com.mskd.flux.utils.extensions.setAppOrientation
import com.mskd.flux.utils.extensions.showSystemBars

class WindowStateHolder(context: Context) {

    private val activity = context.findActivity()

    fun forceScreenOn() {
        //activity?.setAppInLandscape()
        activity?.forceScreenOn(true)
    }

    fun resetOrientation(originalOrientation: Int) {
        activity?.setAppOrientation(originalOrientation)
        activity?.forceScreenOn(false)
    }

    fun updateSystemBars(show: Boolean) {
        if (show) activity?.showSystemBars() else activity?.hideSystemBars()
    }

    fun changeBrightness(delta: Float) : Int? {
        return activity?.let {

            val params = it.window.attributes
            val current = if (params.screenBrightness < 0f) 0.5f else params.screenBrightness

            params.screenBrightness = (current + delta).coerceIn(0f, 1f)
            it.window.attributes = params

            (params.screenBrightness * 100).toInt()

        }
    }

    fun resetBrightness() {
        activity?.let {
            val params = it.window.attributes
            params.screenBrightness = -1f
            it.window.attributes = params
        }
    }

}

@Composable
fun rememberWindowStateHolder(
    context: Context = LocalContext.current,
): WindowStateHolder {
    return remember(context) {
        WindowStateHolder(context = context)
    }
}