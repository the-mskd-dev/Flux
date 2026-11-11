package com.mskd.flux.features.season.ui

import com.mskd.flux.features.season.presentation.SeasonIntent
import com.mskd.flux.ui.media.episode.EpisodeActions

internal fun seasonEpisodeActions(sendIntent: (SeasonIntent) -> Unit) = EpisodeActions(
    onPlay = { sendIntent(SeasonIntent.PlayMedia(media = it)) },
    onChangeWatchStatus = { sendIntent(SeasonIntent.ChangeWatchStatus(media = it)) },
    onOpenInfo = { sendIntent(SeasonIntent.OpenEpisodeInfo(episode = it)) },
    onOpenFileExplorer = { sendIntent(SeasonIntent.OpenFileExplorer(media = it)) },
    onToggleSpoiler = { sendIntent(SeasonIntent.ToggleEpisodeSpoiler(episode = it)) },
    onExpand = { sendIntent(SeasonIntent.ExpandEpisodeDescription(episode = it)) },
    onCollapse = { sendIntent(SeasonIntent.CollapseEpisodeDescription) },
)
