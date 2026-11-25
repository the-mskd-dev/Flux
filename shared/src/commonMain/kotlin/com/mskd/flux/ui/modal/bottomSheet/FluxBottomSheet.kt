package com.mskd.flux.ui.modal.bottomSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mskd.flux.ui.fillMaxWidthWithLimit
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.modal.LocalModalHost
import com.mskd.flux.ui.modal.ModalEntry
import com.mskd.flux.ui.modal.ModalHostState
import com.mskd.flux.ui.modal.scrim.ScrimBackground
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import dev.chrisbanes.haze.HazeState


/** The sheet takes at most this fraction of the available height; its content scrolls beyond. */
private const val MAX_SHEET_HEIGHT_FRACTION = 0.6f

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val maxSheetHeight = maxHeight * MAX_SHEET_HEIGHT_FRACTION

        ScrimBackground(
            isVisible = visible,
            hazeState = hazeState,
            entry = entry
        )

        // Sheet
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Horizontal)
                ),
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
                        .heightIn(max = maxSheetHeight)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                ) {

                    Text.List.Title(
                        modifier = Modifier
                            .padding(top = FluxUI.Space.medium)
                            .padding(horizontal = FluxUI.Space.medium),
                        text = entry?.title,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                    ) {

                        entry?.content(this)

                        Spacer(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                                .padding(vertical = FluxUI.Space.medium)
                        )
                    }

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