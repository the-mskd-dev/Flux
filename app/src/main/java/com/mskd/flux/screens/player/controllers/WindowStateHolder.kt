package com.mskd.flux.screens.player.controllers

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mskd.flux.utils.extensions.changeBrightness
import com.mskd.flux.utils.extensions.findActivity
import com.mskd.flux.utils.extensions.forceScreenOn
import com.mskd.flux.utils.extensions.hideSystemBars
import com.mskd.flux.utils.extensions.resetBrightness
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
        return activity?.changeBrightness(delta = delta)
    }

    fun resetBrightness() {
        activity?.resetBrightness()
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