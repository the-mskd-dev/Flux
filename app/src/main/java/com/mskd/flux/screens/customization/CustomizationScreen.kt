package com.mskd.flux.screens.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.screen.customization.CustomizationDialog
import com.mskd.flux.screen.customization.CustomizationEvent
import com.mskd.flux.screen.customization.CustomizationIntent
import com.mskd.flux.screen.customization.CustomizationUiState
import com.mskd.flux.screen.customization.CustomizationViewModel
import com.mskd.flux.screens.customization.composables.CornersDialog
import com.mskd.flux.screens.customization.composables.CustomizationArtworkSection
import com.mskd.flux.screens.customization.composables.CustomizationGlobalSection
import com.mskd.flux.screens.customization.composables.CustomizationPlayerSection
import com.mskd.flux.screens.customization.composables.CustomizationThemeSection
import com.mskd.flux.screens.customization.composables.ItemsPerRowDialog
import com.mskd.flux.ui.component.global.FluxOptionsDialog
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.resolve
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.customization
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomizationScreen(
    onBack: () -> Unit,
    viewModel: CustomizationViewModel = koinViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                CustomizationEvent.BackToPreviousScreen -> onBack()
            }
        }
    }

    CustomizationContent(
        state = state,
        sendIntent = viewModel::handleIntent
    )

    when (val dialog = state.dialog) {
        is CustomizationDialog.SelectDialog -> {
            FluxOptionsDialog(
                state = dialog.state,
                onValidate = { viewModel.handleIntent(it) },
                onDismiss = { viewModel.handleIntent(CustomizationIntent.HideDialog) }
            )
        }
        is CustomizationDialog.ItemsPerRowDialog -> {

            ItemsPerRowDialog(
                value = state.itemsPerRow,
                title = dialog.title.resolve(),
                description = dialog.desc.resolve(),
                onValidate = { viewModel.handleIntent(CustomizationIntent.SetItemsPerRowValue(it))},
                onDismiss = { viewModel.handleIntent(CustomizationIntent.HideDialog) }
            )
        }
        is CustomizationDialog.SeasonsPerRowDialog -> {
            ItemsPerRowDialog(
                value = state.seasonsPerRow,
                title = dialog.title.resolve(),
                description = dialog.desc.resolve(),
                onValidate = { viewModel.handleIntent(CustomizationIntent.SetSeasonsPerRowValue(it))},
                onDismiss = { viewModel.handleIntent(CustomizationIntent.HideDialog) }
            )
        }
        is CustomizationDialog.ItemsCornersDialog -> {
            CornersDialog(
                value = state.itemsCorners,
                onValidate = { viewModel.handleIntent(CustomizationIntent.SetItemsCornersValue(it))},
                onDismiss = { viewModel.handleIntent(CustomizationIntent.HideDialog) }
            )
        }
        null -> {}
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationContent(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(Res.string.customization),
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onBackTap = { sendIntent(CustomizationIntent.OnBackTap) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            CustomizationThemeSection(
                state = state,
                sendIntent = sendIntent
            )

            CustomizationGlobalSection(
                state = state,
                sendIntent = sendIntent
            )

            CustomizationArtworkSection(
                state = state,
                sendIntent = sendIntent
            )

            CustomizationPlayerSection(
                state = state,
                sendIntent = sendIntent
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}