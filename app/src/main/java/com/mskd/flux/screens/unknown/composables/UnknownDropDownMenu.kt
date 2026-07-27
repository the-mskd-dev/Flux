package com.mskd.flux.screens.unknown.composables

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.features.unknown.presentation.UnknownIntent
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_file_explorer
import flux.shared.generated.resources.open_in_file_explorer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UnknownDropDownMenu(
    episode: Episode,
    onDismissRequest: () -> Unit,
    sendIntent: (UnknownIntent) -> Unit
) {

    val items = buildList {

        // Open in file explorer
        add(
            FluxDropDownMenuItem(
                text = stringResource(Res.string.open_in_file_explorer),
                onClick = {
                    sendIntent(UnknownIntent.OpenFileExplorer(media = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(painter = painterResource(Res.drawable.ic_file_explorer), contentDescription = null)
                },
            )
        )

    }

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = items
    )

}