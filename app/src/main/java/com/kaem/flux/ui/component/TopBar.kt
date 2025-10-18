package com.kaem.flux.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxTopBar(
    text: String,
    onBackButtonTap: () -> Unit
) {

    CenterAlignedTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        title = { Text.Headline.Medium(text = text) },
        navigationIcon = {
            BackButton(onTap = onBackButtonTap)
        }
    )

}