package com.kaem.flux.screens.player.controllers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kaem.flux.utils.extensions.findActivity
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars

class WindowStateHolder(context: Context) {

    private val activity = context.findActivity()

    fun setLandscape() {
        activity?.setAppInLandscape()
        activity?.forceScreenOn(true)
    }

    fun resetOrientation(originalOrientation: Int) {
        activity?.setAppOrientation(originalOrientation)
        activity?.forceScreenOn(false)
    }

    fun updateSystemBars(show: Boolean) {
        if (show) activity?.showSystemBars() else activity?.hideSystemBars()
    }

}

@Composable
fun rememberScreenStateHolder(
    context: Context = LocalContext.current,
): WindowStateHolder {
    return remember(context) {
        WindowStateHolder(context = context)
    }
}