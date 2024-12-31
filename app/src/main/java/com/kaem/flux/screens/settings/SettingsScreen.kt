package com.kaem.flux.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxWeight

@Composable
fun SettingsScreen(
    onBackButtonTap: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        FluxTopBar(
            text = stringResource(R.string.settings),
            onBackButtonTap = onBackButtonTap
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
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = text,

            )

        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

    }

}