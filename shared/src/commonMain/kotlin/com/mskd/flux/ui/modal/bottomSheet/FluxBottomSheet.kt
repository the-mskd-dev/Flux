package com.mskd.flux.ui.modal.bottomSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.blurForeground
import com.mskd.flux.ui.fillMaxWidthWithLimit
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.modal.LocalModalHost
import com.mskd.flux.ui.modal.ModalEntry
import com.mskd.flux.ui.modal.ModalHostState
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import dev.chrisbanes.haze.HazeState


@Composable
fun FluxBottomSheetHost(
    host: ModalHostState,
    hazeState: HazeState,
) {

    val current = host.entry as? ModalEntry.BottomSheet
    val holder = remember { arrayOfNulls<ModalEntry.BottomSheet>(1) }
    if (current != null) holder[0] = current
    val entry = current ?: holder[0]

    val visible = host.visible && current != null

    if (visible) {
        BackGesture(enabled = true, onBack = { entry?.onDismiss?.invoke() })
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Scrim
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .blurForeground(state = hazeState, radius = 3.dp)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { entry?.onDismiss() }
                    )
            )
        }

        // Sheet
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) { it } + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidthWithLimit(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blurForeground(hazeState)
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .9f))
                        .navigationBarsPadding()
                        .padding(vertical = FluxUI.Space.medium),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                ) {

                    Text.List.Title(
                        modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                        text = entry?.title,
                    )

                    entry?.content(this)

                }

            }
        }

    }

}

@Composable
fun FluxBottomSheet(
    isVisible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalModalHost.current
    val owner = remember { Any() }

    SideEffect {
        if (isVisible) {
            host.owner = owner
            host.entry = ModalEntry.BottomSheet(title, onDismiss, content)
            host.visible = true
        } else if (host.owner === owner) {
            host.visible = false
        }
    }

    DisposableEffect(owner) {
        onDispose { if (host.owner === owner) host.visible = false }
    }
}