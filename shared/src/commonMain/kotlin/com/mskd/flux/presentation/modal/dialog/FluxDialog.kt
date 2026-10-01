package com.mskd.flux.presentation.modal.dialog

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.Text
import com.mskd.flux.presentation.blurForeground
import com.mskd.flux.presentation.fillMaxWidthWithLimit
import com.mskd.flux.presentation.modal.LocalModalHost
import com.mskd.flux.presentation.modal.ModalEntry
import com.mskd.flux.presentation.modal.ModalHostState
import dev.chrisbanes.haze.HazeState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.cancel
import flux.shared.generated.resources.validate
import org.jetbrains.compose.resources.stringResource

@Composable
fun FluxDialogHost(
    host: ModalHostState,
    hazeState: HazeState,
) {

    val entry = host.entry as? ModalEntry.Dialog ?: return

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
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = FluxUI.Space.large)
            ,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidthWithLimit()
                    .imePadding(),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blurForeground(hazeState)
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .8f))
                        .padding(FluxUI.Space.large),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
                ) {

                    Text.Content.Title(
                        text = entry.title,
                    )

                    entry.content(this)

                    Row(
                        modifier = Modifier
                            .padding(top = FluxUI.Space.small)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small, Alignment.End),
                    ) {

                        TextButton(onClick = entry.onDismiss) { Text.Button.Default(text = entry.onDismissLabel) }

                        entry.onValidate?.let {
                            TextButton(onClick = it) { Text.Button.Default(text = entry.onValidateLabel) }
                        }

                    }

                }

            }

        }

    }

}

/**
 * Simple AlertDialog with Cancel and Validate buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDismissLabel: String = stringResource(Res.string.cancel),
    onValidate: (() -> Unit)? = null,
    onValidateLabel: String = stringResource(Res.string.validate),
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    val host = LocalModalHost.current
    val owner = remember { Any() }

    SideEffect {
        if (isVisible) {
            host.owner = owner
            host.entry = ModalEntry.Dialog(
                title = title,
                onDismiss = onDismiss,
                onDismissLabel = onDismissLabel,
                onValidate = onValidate,
                onValidateLabel = onValidateLabel,
                content = content
            )
            host.visible = true
        } else if (host.owner === owner) {
            host.visible = false
        }
    }

    DisposableEffect(owner) {
        onDispose { if (host.owner === owner) host.visible = false }
    }

}