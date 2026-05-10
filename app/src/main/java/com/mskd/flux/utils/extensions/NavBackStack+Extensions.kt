package com.mskd.flux.utils.extensions

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.popScreen() {
    if (this.size > 1) this.removeLastOrNull()
}