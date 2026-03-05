package com.kaem.flux.utils.extensions

import com.kaem.flux.screens.player.PlayerTrack
import java.util.Locale


fun Locale.toPlayerTrack(type: PlayerTrack.Type) = PlayerTrack(
    id = null,
    label = this.displayName,
    language = this.language,
    type = type
)