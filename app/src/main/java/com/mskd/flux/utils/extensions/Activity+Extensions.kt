package com.mskd.flux.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mskd.flux.screens.player.PlayerIntent.UpdateAmbientOverlay
import com.mskd.flux.screens.player.PlayerUiState

fun Activity.hideSystemBars() {

    val windowInsets = WindowInsetsControllerCompat(this.window, this.window.decorView)
    windowInsets.hide(WindowInsetsCompat.Type.systemBars())

}

fun Activity.showSystemBars() {

    val windowInsets = WindowInsetsControllerCompat(this.window, this.window.decorView)
    windowInsets.show(WindowInsetsCompat.Type.systemBars())

}

fun Activity.setAppInLandscape() {
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.setAppInPortrait() {
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.setAppOrientation(orientation: Int) {
    this.requestedOrientation = orientation
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

fun Activity.forceScreenOn(force: Boolean) {
    if (force)
        this.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    else
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun Activity.changeBrightness(delta: Float) : Int {
    val params = this.window.attributes
    val current = if (params.screenBrightness < 0f) 0.5f else params.screenBrightness

    params.screenBrightness = (current + delta).coerceIn(0f, 1f)
    this.window.attributes = params

    return (params.screenBrightness * 100).toInt()
}
