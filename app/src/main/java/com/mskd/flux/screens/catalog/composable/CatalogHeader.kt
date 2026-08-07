package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogHeader() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FluxUI.Space.small),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(Res.drawable.ic_flux),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Flux icon"
        )
    }

}