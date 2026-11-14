package com.mskd.flux.features.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.filters
import flux.shared.generated.resources.ic_filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The single filter button of the Gallery: it opens the sheet holding the type and language filters. */
@Composable
fun GalleryFilters(
    sendIntent: (GalleryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {

        FilledTonalButton(onClick = { sendIntent(GalleryIntent.OnFilterTap) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_filter),
                contentDescription = stringResource(Res.string.filters)
            )
        }

    }

}
