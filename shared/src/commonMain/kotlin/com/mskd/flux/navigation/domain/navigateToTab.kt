package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavKey

fun MutableList<NavKey>.navigateToTab(
    target: Route,
) {
    val current = this.lastOrNull() as? Route
    if (current.isSameTabAs(target)) return

    val existingIndex = this.indexOfFirst { (it as? Route).isSameTabAs(target) }
    if (existingIndex != -1) {
        while (this.size > existingIndex + 1) this.removeAt(this.lastIndex)
    } else {
        this.add(target)
    }
}