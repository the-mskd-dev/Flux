package com.kaem.flux.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

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
