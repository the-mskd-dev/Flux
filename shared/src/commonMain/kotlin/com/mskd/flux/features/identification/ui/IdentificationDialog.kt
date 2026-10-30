package com.mskd.flux.features.identification.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.identification.presentation.IdentificationCandidate
import com.mskd.flux.features.identification.presentation.IdentificationEvent
import com.mskd.flux.features.identification.presentation.IdentificationIntent
import com.mskd.flux.features.identification.presentation.IdentificationUiState
import com.mskd.flux.features.identification.presentation.IdentificationViewModel
import com.mskd.flux.ui.components.FluxTextButton
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.modal.dialog.FluxDialog
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.enter_search
import flux.shared.generated.resources.ic_images
import flux.shared.generated.resources.identification_current
import flux.shared.generated.resources.identification_failed
import flux.shared.generated.resources.identification_no_result
import flux.shared.generated.resources.identification_search_failed
import flux.shared.generated.resources.identification_select_data
import flux.shared.generated.resources.retry
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdentificationDialogContent(
    uiState: IdentificationUiState,
    sendIntent: (IdentificationIntent) -> Unit,
) {

    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.query,
            onValueChange = { sendIntent(IdentificationIntent.UpdateQuery(it)) },
            enabled = !uiState.isIdentifying,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text.Content.Body(text = stringResource(Res.string.enter_search)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { sendIntent(IdentificationIntent.Search) }),
        )

        if (uiState.identificationFailed) {
            Text.Content.Label(
                text = stringResource(Res.string.identification_failed),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isIdentifying || uiState.results is State.Loading -> LoadingIndicator()
                uiState.results is State.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                ) {
                    Text.Content.Body(text = stringResource(Res.string.identification_search_failed))
                    FluxTextButton(
                        text = stringResource(Res.string.retry),
                        onClick = { sendIntent(IdentificationIntent.Search) },
                    )
                }
                uiState.results is State.Content && uiState.results.content.isEmpty() ->
                    Text.Content.Body(text = stringResource(Res.string.identification_no_result))
                uiState.results is State.Content -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                ) {
                    items(items = uiState.results.content, key = { it.artwork.id }) { candidate ->
                        CandidateItem(
                            candidate = candidate,
                            onClick = { sendIntent(IdentificationIntent.Select(candidate)) },
                        )
                    }
                }
            }
        }

    }

}

@Composable
private fun CandidateItem(
    candidate: IdentificationCandidate,
    onClick: () -> Unit,
) {

    val placeholder = rememberIconPlaceholderPainter(icon = Res.drawable.ic_images)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !candidate.isCurrent, onClick = onClick)
            .alpha(if (candidate.isCurrent) .5f else 1f)
            .padding(FluxUI.Space.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
    ) {

        FluxImage(
            modifier = Modifier
                .width(56.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp)),
            path = candidate.artwork.imagePath,
            contentDescription = candidate.artwork.title,
            placeholder = placeholder,
            error = placeholder,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
        ) {
            Text.List.Title(text = candidate.artwork.title, maxLines = 2)
            Text.List.Body(text = candidate.year?.toString())
            if (candidate.isCurrent) {
                Text.Content.Label(
                    text = stringResource(Res.string.identification_current),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

    }

}
