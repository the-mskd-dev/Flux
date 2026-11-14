package com.mskd.flux.features.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import com.mskd.flux.ui.components.FluxFilterChip
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.gallery_filter_language
import flux.shared.generated.resources.gallery_filter_type
import org.jetbrains.compose.resources.stringResource

/** The filter chips of the Gallery, presented like the ones of the Search page. */
@Composable
fun GalleryFilters(
    selectedTypesCount: Int,
    selectedLanguagesCount: Int,
    sendIntent: (GalleryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        FluxFilterChip(
            text = stringResource(Res.string.gallery_filter_type) + if (selectedTypesCount > 0) " ($selectedTypesCount)" else "",
            selected = selectedTypesCount > 0,
            onClick = { sendIntent(GalleryIntent.OnFilterTap) },
        )

        FluxFilterChip(
            text = stringResource(Res.string.gallery_filter_language) + if (selectedLanguagesCount > 0) " ($selectedLanguagesCount)" else "",
            selected = selectedLanguagesCount > 0,
            onClick = { sendIntent(GalleryIntent.OnFilterTap) },
        )

    }

}
