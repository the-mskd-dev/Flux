package com.mskd.flux.ui.modal.scrim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.blurForeground
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.modal.LocalModalHost
import com.mskd.flux.ui.modal.ModalEntry
import com.mskd.flux.ui.modal.ModalHostState
import dev.chrisbanes.haze.HazeState

@Composable
fun FluxScrimHost(
    host: ModalHostState,
    hazeState: HazeState,
) {

    val current = host.entry as? ModalEntry.Scrim
    val holder = remember { arrayOfNulls<ModalEntry.Scrim>(1) }
    if (current != null) holder[0] = current
    val entry = current ?: holder[0]

    val visible = host.visible && current != null

    if (visible) {
        BackGesture(enabled = true, onBack = { entry?.onDismiss?.invoke() })
    }

    ScrimBackground(
        isVisible = visible,
        hazeState = hazeState,
        entry = entry
    )

}

/**
 * Simple AlertDialog with Cancel and Validate buttons
 */
@Composable
fun FluxScrim(
    isVisible: Boolean = true,
    onDismiss: () -> Unit,
) {

    val host = LocalModalHost.current
    val owner = remember { Any() }

    SideEffect {
        if (isVisible) {
            host.owner = owner
            host.entry = ModalEntry.Scrim(onDismiss = onDismiss,)
            host.visible = true
        } else if (host.owner === owner) {
            host.visible = false
        }
    }

    DisposableEffect(owner) {
        onDispose { if (host.owner === owner) host.visible = false }
    }

}