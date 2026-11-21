package com.mskd.flux.ui.media.episode

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Episode

/** What the user can do on an Episode of an Artwork page: each page maps these actions to its own intents. */
@Immutable
class EpisodeActions(
    val onPlay: (Episode) -> Unit,
    val onChangeWatchStatus: (Episode) -> Unit,
    val onOpenInfo: (Episode) -> Unit,
    val onOpenFileExplorer: (Episode) -> Unit,
    val onToggleSpoiler: (Episode) -> Unit,
    val onExpand: (Episode) -> Unit,
    val onCollapse: () -> Unit,
)
