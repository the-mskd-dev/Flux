package com.mskd.flux.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FluxDivider() {

    HorizontalDivider(
        modifier = Modifier
            .height(2.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    )

}