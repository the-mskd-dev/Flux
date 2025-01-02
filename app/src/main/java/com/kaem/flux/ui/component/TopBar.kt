package com.kaem.flux.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FluxTopBar(
    text: String,
    onBackButtonTap: () -> Unit
) {

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {

        BackButton(onTap = onBackButtonTap)

        BoldText(
            modifier = Modifier.align(Alignment.Center),
            text = text
        )

    }
}