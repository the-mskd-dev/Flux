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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.BackButton
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