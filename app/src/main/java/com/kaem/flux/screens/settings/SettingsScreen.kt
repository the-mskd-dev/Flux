package com.kaem.flux.screens.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.component.LightText
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.theme.Ui

@Composable
fun SettingsScreen(
    onBackButtonTap: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        FluxTopBar(
            text = stringResource(R.string.settings),
            onBackButtonTap = onBackButtonTap
        )

        SettingsItem(
            text = "Thème de l'application",
            value = stringResource(state.uiTheme.stringResourceId),
            onTap = { viewModel.showUiThemeDialog(true) }
        )

        SettingsItem(
            text = "Bouton arrière",
            value = "${state.backwardValue}sec",
            onTap = { viewModel.showBackwardDialog(true) }
        )

        SettingsItem(
            text = "Bouton avant",
            value = "${state.forwardValue}sec",
            onTap = { viewModel.showForwardDialog(true) }
        )

    }

    SettingsDialog(
        show = state.showBackwardDialog,
        currentValue = state.backwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.setBackwardValue(it) },
        onDismiss = { viewModel.showBackwardDialog(false) }
    )

    SettingsDialog(
        show = state.showForwardDialog,
        currentValue = state.forwardValue,
        options = SettingsViewModel.playerSeconds,
        onSelect = { viewModel.setForwardValue(it) },
        onDismiss = { viewModel.showForwardDialog(false) }
    )

    SettingsDialog(
        show = state.showUiThemeDialog,
        currentValue = state.uiTheme,
        options = mapOf(
            Ui.THEME.LIGHT to stringResource(Ui.THEME.LIGHT.stringResourceId),
            Ui.THEME.DARK to stringResource(Ui.THEME.DARK.stringResourceId),
            Ui.THEME.SYSTEM to stringResource(Ui.THEME.SYSTEM.stringResourceId),
        ),
        onSelect = { viewModel.setUiTheme(it) },
        onDismiss = { viewModel.showUiThemeDialog(false) }
    )

}

@Composable
fun SettingsItem(
    text: String,
    value: String,
    onTap: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable { onTap() }
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.LARGE)
    ) {

        MediumText(
            text = text,
            fontSize = Ui.FontSize.LARGE,
        )

        LightText(
            text = value,
            fontSize = Ui.FontSize.SMALL,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
        )

    }

}

@Composable
fun <T> SettingsDialog(
    show: Boolean,
    currentValue: T,
    options: Map<T, String>,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedValue by remember { mutableStateOf(currentValue) }

    FluxDialog(
        show = show,
        onDismissRequest = onDismiss,
        onValidate = {
            onSelect(selectedValue)
            onDismiss()
        },
        content = {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clickable { selectedValue = option.key }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = selectedValue == option.key,
                            onClick = { selectedValue = option.key }
                        )

                        MediumText(
                            modifier = Modifier.weight(1f),
                            text = option.value,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }

            }

        }
    )

}