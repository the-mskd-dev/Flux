package com.mskd.flux.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
class SystemBarsAppearanceState {
    private val lightIconTokens = mutableStateListOf<Any>()

    val forceLightIcons: Boolean
        get() = lightIconTokens.isNotEmpty()

    var enforceNavigationBarContrast by mutableStateOf(true)

    fun pushForceLightIcons(token: Any) {
        if (token !in lightIconTokens) lightIconTokens.add(token)
    }

    fun popForceLightIcons(token: Any) {
        lightIconTokens.remove(token)
    }
}

val LocalSystemBarsAppearance = staticCompositionLocalOf { SystemBarsAppearanceState() }

@Composable
fun ForceLightSystemBarIcons(enabled: Boolean = true) {
    val state = LocalSystemBarsAppearance.current
    val token = remember { Any() }
    DisposableEffect(state, token, enabled) {
        if (enabled) state.pushForceLightIcons(token)
        onDispose { state.popForceLightIcons(token) }
    }
}
