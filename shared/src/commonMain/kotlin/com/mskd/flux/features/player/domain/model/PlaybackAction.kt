package com.mskd.flux.features.player.domain.model

import com.mskd.flux.core.model.artwork.Media

sealed interface PlaybackAction {
    data class OpenInternalPlayer(val mediaId: Long) : PlaybackAction
    data class OpenExternalPlayer(val media: Media) : PlaybackAction
    data object Unavailable : PlaybackAction
}