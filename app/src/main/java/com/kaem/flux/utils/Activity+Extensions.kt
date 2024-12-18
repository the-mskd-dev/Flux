package com.kaem.flux.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController

fun Activity.hideSystemBars() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.window.insetsController?.let { controller ->
            controller.hide(android.view.WindowInsets.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        this.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                )

    }

}

fun Activity.showSystemBars() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.window.insetsController?.show(android.view.WindowInsets.Type.systemBars())
    } else {
        @Suppress("DEPRECATION")
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

}