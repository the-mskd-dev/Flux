package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_eraser
import flux.shared.generated.resources.ic_file_explorer
import flux.shared.generated.resources.more_info
import flux.shared.generated.resources.open_in_file_explorer
import flux.shared.generated.resources.reset_progress
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtworkDropDownMenu(
    fullArtwork: FullArtwork,
    onDismissRequest: () -> Unit,
    sendIntent: (ArtworkIntent) -> Unit
) {

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = buildList {

            // More Info
            add(
                FluxDropDownMenuItem(
                    text = stringResource(Res.string.more_info),
                    onClick = {
                        sendIntent(ArtworkIntent.OpenArtworkInfo)
                        onDismissRequest()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(Res.string.more_info)
                        )
                    },
                )
            )

            // Reset progress
            add(
                FluxDropDownMenuItem(
                    text = stringResource(Res.string.reset_progress),
                    onClick = {
                        sendIntent(ArtworkIntent.ShowResetProgressDialog)
                        onDismissRequest()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_eraser),
                            contentDescription = stringResource(Res.string.reset_progress)
                        )
                    },
                )
            )

            // Open in file explorer (only for movies)
            if (fullArtwork is FullArtwork.FullMovie) {
                add(
                    FluxDropDownMenuItem(
                        text = stringResource(Res.string.open_in_file_explorer),
                        onClick = {
                            sendIntent(ArtworkIntent.OpenFileExplorer(media = fullArtwork.movie))
                            onDismissRequest()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_file_explorer),
                                contentDescription = stringResource(Res.string.reset_progress)
                            )
                        },
                    )
                )
            }
        }
    )

}