package com.mskd.flux.presentation.bottomSheet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.Text
import com.mskd.flux.presentation.blurForeground
import com.mskd.flux.presentation.fillMaxWidthWithLimit
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState


@Stable
class SheetHostState {
    var visible by mutableStateOf(false)
        internal set
    internal var entry by mutableStateOf<SheetEntry?>(null)
    internal var owner: Any? = null
}

internal class SheetEntry(
    val title: String?,
    val onDismiss: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)

val LocalSheetHost = staticCompositionLocalOf<SheetHostState> {
    error("SheetHostState not provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxBottomSheetHost(
    host: SheetHostState,
    hazeState: HazeState,
) {

    val entry = host.entry ?: return

    BackHandler(enabled = host.visible, onBack = entry.onDismiss)

    Box(modifier = Modifier.fillMaxSize()) {

        // Scrim
        AnimatedVisibility(
            visible = host.visible,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = entry.onDismiss
                    )
            )
        }

        // Sheet
        AnimatedVisibility(
            visible = host.visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidthWithLimit(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                        text = entry.title,
                    )

                    entry.content(this)

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
    val host = LocalSheetHost.current
    val owner = remember { Any() }

    SideEffect {
        if (isVisible) {
            host.owner = owner
            host.entry = SheetEntry(title, onDismiss, content) // contenu rafraîchi à chaque recomposition
            host.visible = true
        } else if (host.owner === owner) {
            host.visible = false
        }
    }

    DisposableEffect(owner) {
        onDispose { if (host.owner === owner) host.visible = false }
    }
}