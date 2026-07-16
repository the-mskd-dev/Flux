package com.mskd.flux.screens.catalog.composable

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.utils.FluxSnackbar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogSnackbar(
    snackbarState: FluxSnackbar?,
    snackbarHostState: SnackbarHostState,
    sendIntent: (CatalogIntent) -> Unit
) {

    val message = snackbarState?.message?.let { stringResource(it) }.orEmpty()
    val actionLabel = snackbarState?.action?.let { stringResource(it) }.orEmpty()

    LaunchedEffect(snackbarState) {
        if (snackbarState != null) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )

            when (result) {
                SnackbarResult.ActionPerformed -> sendIntent(CatalogIntent.OnSnackbarActionTap)
                SnackbarResult.Dismissed -> sendIntent(CatalogIntent.OnDismissSnackbar)
            }
        }
    }

}