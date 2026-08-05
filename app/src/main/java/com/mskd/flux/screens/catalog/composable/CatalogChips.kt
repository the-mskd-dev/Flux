package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.features.catalog.domain.model.CatalogSorting
import com.mskd.flux.features.catalog.domain.model.CatalogSortingOption
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.resolve

@Composable
fun CatalogChips(
    sortingOption: CatalogSortingOption,
    sendIntent: (CatalogIntent) -> Unit
) {

    Row(
        modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {

        AssistChip(
            onClick = { sendIntent(CatalogIntent.ShowSortingOptions(show = true)) },
            label = { Text.Button.Chip(sortingOption.description.resolve()) },
            shape = CircleShape
        )

    }

}