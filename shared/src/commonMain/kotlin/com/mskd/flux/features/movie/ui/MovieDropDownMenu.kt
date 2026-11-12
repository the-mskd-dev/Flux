package com.mskd.flux.features.movie.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.movie.presentation.MovieIntent
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.ui.modal.menu.FluxPopUpMenu
import com.mskd.flux.ui.modal.menu.FluxPopUpMenuItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_eraser
import flux.shared.generated.resources.ic_file_explorer
import flux.shared.generated.resources.ic_search
import flux.shared.generated.resources.identification_select_data
import flux.shared.generated.resources.more_info
import flux.shared.generated.resources.open_in_file_explorer
import flux.shared.generated.resources.reset_progress
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MovieDropDownMenu(
    isVisible: Boolean,
    fullMovie: FullArtwork.FullMovie,
    identification: IdentificationAvailability,
    onDismissRequest: () -> Unit,
    sendIntent: (MovieIntent) -> Unit
) {

    FluxPopUpMenu(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        items = buildList {

            // More Info
            add(
                FluxPopUpMenuItem(
                    text = stringResource(Res.string.more_info),
                    onClick = {
                        sendIntent(MovieIntent.OpenMovieInfo)
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
                FluxPopUpMenuItem(
                    text = stringResource(Res.string.reset_progress),
                    onClick = {
                        sendIntent(MovieIntent.ShowResetProgressDialog)
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

            // Manual identification
            if (identification != IdentificationAvailability.HIDDEN) {
                add(
                    FluxPopUpMenuItem(
                        text = stringResource(Res.string.identification_select_data),
                        onClick = {
                            sendIntent(MovieIntent.ShowIdentificationDialog)
                            onDismissRequest()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_search),
                                contentDescription = stringResource(Res.string.identification_select_data)
                            )
                        },
                        enabled = identification == IdentificationAvailability.ENABLED,
                    )
                )
            }

            // Open in file explorer
            add(
                FluxPopUpMenuItem(
                    text = stringResource(Res.string.open_in_file_explorer),
                    onClick = {
                        sendIntent(MovieIntent.OpenFileExplorer(media = fullMovie.movie))
                        onDismissRequest()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_file_explorer),
                            contentDescription = stringResource(Res.string.open_in_file_explorer)
                        )
                    },
                )
            )
        }
    )

}