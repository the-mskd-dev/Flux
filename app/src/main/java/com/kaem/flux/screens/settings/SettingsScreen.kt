package com.kaem.flux.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight

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
        currentValue = state.backwardValue.toString(),
        values = listOf("5", "10", "15", "20", "25", "30"),
        onSelect = { viewModel.setBackwardValue(it.toInt()) },
        onDismiss = { viewModel.showBackwardDialog(false) }
    )

    SettingsDialog(
        show = state.showForwardDialog,
        currentValue = state.forwardValue.toString(),
        values = listOf("5", "10", "15", "20", "25", "30"),
        onSelect = { viewModel.setForwardValue(it.toInt()) },
        onDismiss = { viewModel.showForwardDialog(false) }
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
            .padding(horizontal = FluxSpace.MEDIUM, vertical = FluxSpace.LARGE)
    ) {

        Text(
            text = text,
            fontWeight = FluxWeight.LIGHT,
            fontSize = FluxFontSize.LARGE,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
            fontWeight = FluxWeight.LIGHT,
            fontSize = FluxFontSize.SMALL,
        )

    }

}

@Composable
fun SettingsDialog(
    show: Boolean,
    currentValue: String,
    values: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedValue by remember { mutableStateOf(currentValue) }

    if (show) {

        Dialog(
            onDismissRequest = onDismiss
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
            ) {

                values.forEach { value ->

                    Row(
                        modifier = Modifier
                            .clickable { selectedValue = value }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FluxSpace.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = selectedValue == value,
                            onClick = { selectedValue = value }
                        )

                        Text(
                            modifier = Modifier.weight(1f),
                            text = value,
                            fontWeight = FluxWeight.LIGHT,
                            fontSize = FluxFontSize.LARGE,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }

                FluxButton(
                    modifier = Modifier.align(Alignment.End),
                    text = "Valider",
                    onTap = { onSelect(selectedValue); onDismiss() }
                )

            }

        }

    }

}