package com.mskd.flux.ui.modal.scrim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.blurForeground
import com.mskd.flux.ui.modal.ModalEntry
import dev.chrisbanes.haze.HazeState

@Composable
internal fun ScrimBackground(
    isVisible: Boolean,
    hazeState: HazeState,
    entry: ModalEntry?
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .blurForeground(state = hazeState, radius = 3.dp)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = .4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { entry?.onDismiss() }
                )
        )
    }
}