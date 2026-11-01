package com.mskd.flux.features.identification.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.features.identification.presentation.IdentificationEvent
import com.mskd.flux.features.identification.presentation.IdentificationIntent
import com.mskd.flux.features.identification.presentation.IdentificationUiState
import com.mskd.flux.features.identification.presentation.IdentificationViewModel
import com.mskd.flux.features.identification.ui.composables.IdentificationResults
import com.mskd.flux.features.identification.ui.composables.IdentificationSearchField
import com.mskd.flux.ui.modal.dialog.FluxDialog
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.identification_failed
import flux.shared.generated.resources.identification_select_data
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Lists the TMDB results for the Artwork [artworkId]: choosing one performs an Identification manuelle. */
@Composable
fun IdentificationDialog(
    artworkId: Long,
    onDismiss: () -> Unit,
    onIdentified: (artworkId: Long) -> Unit,
    viewModel: IdentificationViewModel = koinViewModel(key = "identification_$artworkId", parameters = { parametersOf(artworkId) }),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is IdentificationEvent.Identified -> onIdentified(event.artworkId)
            }
        }
    }

    FluxDialog(
        title = stringResource(Res.string.identification_select_data),
        // The Médias are being moved: the dialog stays until the end
        onDismiss = { if (!uiState.isIdentifying) onDismiss() },
        content = {
            IdentificationDialogContent(
                uiState = uiState,
                sendIntent = viewModel::handleIntent,
            )
        },
    )

}

@Composable
private fun IdentificationDialogContent(
    uiState: IdentificationUiState,
    sendIntent: (IdentificationIntent) -> Unit,
) {

    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

        IdentificationSearchField(
            query = uiState.query,
            enabled = !uiState.isIdentifying,
            onQueryChange = { sendIntent(IdentificationIntent.UpdateQuery(it)) },
            onSearch = { sendIntent(IdentificationIntent.Search) },
        )

        if (uiState.identificationFailed) {
            IdentificationFailedLabel()
        }

        IdentificationResults(
            results = uiState.results,
            isIdentifying = uiState.isIdentifying,
            onRetry = { sendIntent(IdentificationIntent.Search) },
            onSelect = { sendIntent(IdentificationIntent.Select(it)) },
        )

    }

}

@Composable
private fun IdentificationFailedLabel() {

    Text.Content.Label(
        text = stringResource(Res.string.identification_failed),
        color = MaterialTheme.colorScheme.error,
    )

}
