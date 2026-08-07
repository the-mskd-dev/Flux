package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.empty_catalog
import flux.shared.generated.resources.empty_catalog_desc
import flux.shared.generated.resources.how_to_name_files
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogEmptyContent(sendIntent: (CatalogIntent) -> Unit) {

    Column(
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Text.Content.Title(
            modifier = Modifier.padding(top = FluxUI.Space.medium),
            text = stringResource(Res.string.empty_catalog)
        )

        Text.Content.Body(
            text = stringResource(Res.string.empty_catalog_desc)
        )

        TextButton(
            onClick = { sendIntent(CatalogIntent.OnHowToTap) },
            contentPadding = PaddingValues(vertical = FluxUI.Space.medium)
        ) {
            Text.Button.Default(text = stringResource(Res.string.how_to_name_files),)
        }

    }
}

@Preview
@Composable
fun CatalogEmptyContent_Preview() {
    FluxThemePreview {
        CatalogEmptyContent {  }
    }
}