package com.kaem.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight

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

        MediumText(
            modifier = Modifier.align(Alignment.Center),
            text = text
        )

    }
}