package com.mskd.flux.screens.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.screens.customization.composables.CustomizationThemeSection
import com.mskd.flux.ui.component.FluxOptionsDialog
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.theme.Ui

@Composable
fun CustomizationScreen(
    onBack: () -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel()
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

    state.dialogState?.let { dialogState ->
        FluxOptionsDialog(
            state = dialogState,
            onValidate = { viewModel.handleIntent(it) },
            onDismiss = { viewModel.handleIntent(CustomizationIntent.HideDialog) }
        )
    }

}

@Composable
fun CustomizationContent(
    state: CustomizationUiState,
    sendIntent: (CustomizationIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.customization),
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
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            CustomizationThemeSection(
                state = state,
                sendIntent = sendIntent
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}