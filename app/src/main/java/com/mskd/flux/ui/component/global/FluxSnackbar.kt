package com.mskd.flux.ui.component.global

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mskd.flux.utils.FluxSnackbar
import org.jetbrains.compose.resources.stringResource

@Composable
fun FluxSnackbar(
    snackbarState: FluxSnackbar?,
    snackbarHostState: SnackbarHostState,
    duration: SnackbarDuration = SnackbarDuration.Indefinite,
    withDismissAction: Boolean = true,
    onAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    val message = snackbarState?.message?.let { stringResource(it) }.orEmpty()
    val actionLabel = snackbarState?.action?.let { stringResource(it) }.orEmpty()

    LaunchedEffect(snackbarState) {
        if (snackbarState != null) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration
            )

            when (result) {
                SnackbarResult.ActionPerformed -> onAction()
                SnackbarResult.Dismissed -> onDismiss()
            }
        }
    }

}