package com.mskd.flux.features.artwork.ui

import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.ui.media.episode.EpisodeActions

internal fun artworkEpisodeActions(sendIntent: (ArtworkIntent) -> Unit) = EpisodeActions(
    onPlay = { sendIntent(ArtworkIntent.PlayMedia(media = it)) },
    onChangeWatchStatus = { sendIntent(ArtworkIntent.ChangeWatchStatus(media = it)) },
    onOpenInfo = { sendIntent(ArtworkIntent.OpenEpisodeInfo(episode = it)) },
    onOpenFileExplorer = { sendIntent(ArtworkIntent.OpenFileExplorer(media = it)) },
    onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleEpisodeSpoiler(episode = it)) },
    onExpand = { sendIntent(ArtworkIntent.ExpandEpisodeDescription(episode = it)) },
    onCollapse = { sendIntent(ArtworkIntent.CollapseEpisodeDescription) },
)
