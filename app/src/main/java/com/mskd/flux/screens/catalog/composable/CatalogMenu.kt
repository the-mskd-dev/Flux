package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_source
import flux.shared.generated.resources.ic_add_folder
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.tmdb_api_token
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogMenu() {

    val items = listOf(
        stringResource(Res.string.other_files),
        stringResource(Res.string.add_source),
        "add token",
    )

    FlowRow(
        modifier = Modifier
            .padding(horizontal = FluxUI.Space.medium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        items.forEach {
            CatalogMenuItem(
                text = it,
                icon = painterResource(Res.drawable.ic_add_folder),
                onClick = {}
            )
        }

    }

}

@Composable
fun CatalogMenuItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit
) {

    /*AssistChip(
        onClick = onClick
    )*/


}

@Composable
fun CatalogMenu_Preview() {
    FluxThemePreview {
        Box(Modifier.fillMaxSize()) {
            CatalogMenu()
        }
    }
}