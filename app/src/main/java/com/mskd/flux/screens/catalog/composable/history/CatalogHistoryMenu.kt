package com.mskd.flux.screens.catalog.composable.history

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.delete
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.play
import flux.shared.generated.resources.resume
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogHistoryMenu(
    entry: HistoryEntry,
    onDismissRequest: () -> Unit,
    sendIntent: (CatalogIntent) -> Unit
) {

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = listOf(

            // Play
            FluxDropDownMenuItem(
                text = stringResource(Res.string.resume),
                onClick = {
                    onDismissRequest()
                    sendIntent(CatalogIntent.PlayMedia(media = entry.media))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_play),
                        contentDescription = stringResource(Res.string.play)
                    )
                },
            ),

            // Delete
            FluxDropDownMenuItem(
                text = stringResource(Res.string.delete),
                onClick = { sendIntent(CatalogIntent.DeleteHistoryEntry(entry = entry)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = stringResource(Res.string.delete)
                    )
                },
            )

        )
    )

}