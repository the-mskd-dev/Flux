package com.mskd.flux.ui.component.media

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_file_explorer
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.ic_replay
import flux.shared.generated.resources.ic_visibility
import flux.shared.generated.resources.mark_as_not_watched
import flux.shared.generated.resources.mark_as_watched
import flux.shared.generated.resources.more_info
import flux.shared.generated.resources.open_in_file_explorer
import flux.shared.generated.resources.play
import flux.shared.generated.resources.resume
import flux.shared.generated.resources.rewatch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EpisodeDropDownMenu(
    episode: Episode,
    onDismissRequest: () -> Unit,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val items = buildList {

        if (episode.isAvailable) {

            val text = when (episode.status) {
                Status.WATCHED -> stringResource(Res.string.rewatch)
                Status.IS_WATCHING -> stringResource(Res.string.resume)
                else -> stringResource(Res.string.play)
            }

            // Play
            add(
                FluxDropDownMenuItem(
                    text = text,
                    onClick = {
                        sendIntent(ArtworkIntent.PlayMedia(media = episode))
                        onDismissRequest()
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(if (episode.status == Status.WATCHED) Res.drawable.ic_replay else Res.drawable.ic_play), contentDescription = null)
                    },
                )
            )

        }

        // Status
        add(
            FluxDropDownMenuItem(
                text = if (episode.status == Status.WATCHED) stringResource(Res.string.mark_as_not_watched) else stringResource(Res.string.mark_as_watched),
                onClick = {
                    sendIntent(ArtworkIntent.ChangeWatchStatus(media = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    if (episode.status == Status.WATCHED)
                        Icon(painter = painterResource(Res.drawable.ic_visibility), contentDescription = null)
                    else
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                },
            )
        )

        // More info
        add(
            FluxDropDownMenuItem(
                text = stringResource(Res.string.more_info),
                onClick = {
                    sendIntent(ArtworkIntent.OpenEpisodeInfo(episode = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                },
            )
        )

        // Open in file explorer
        add(
            FluxDropDownMenuItem(
                text = stringResource(Res.string.open_in_file_explorer),
                onClick = {
                    sendIntent(ArtworkIntent.OpenFileExplorer(media = episode))
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