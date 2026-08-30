package com.mskd.flux.features.player.domain.model

import com.mskd.flux.core.model.artwork.Media

sealed interface PlaybackAction {
    data class OpenPlayer(val media: Media, val externalPlayer: Boolean) : PlaybackAction
    data object Unavailable : PlaybackAction
}