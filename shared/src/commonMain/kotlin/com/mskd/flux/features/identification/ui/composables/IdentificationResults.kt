package com.mskd.flux.features.identification.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.identification.presentation.IdentificationCandidate
import com.mskd.flux.ui.components.FluxTextButton
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.identification_no_result
import flux.shared.generated.resources.identification_search_failed
import flux.shared.generated.resources.retry
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IdentificationResults(
    results: State<ImmutableList<IdentificationCandidate>>,
    isIdentifying: Boolean,
    onRetry: () -> Unit,
    onSelect: (IdentificationCandidate) -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isIdentifying || results is State.Loading -> LoadingIndicator()
            results is State.Error -> SearchError(onRetry = onRetry)
            results is State.Content && results.content.isEmpty() ->
                Text.Content.Body(text = stringResource(Res.string.identification_no_result))
            results is State.Content -> IdentificationCandidateList(
                candidates = results.content,
                onSelect = onSelect,
            )
        }
    }

}

@Composable
private fun SearchError(onRetry: () -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
    ) {
        Text.Content.Body(text = stringResource(Res.string.identification_search_failed))
        FluxTextButton(
            text = stringResource(Res.string.retry),
            onClick = onRetry,
        )
    }

}
