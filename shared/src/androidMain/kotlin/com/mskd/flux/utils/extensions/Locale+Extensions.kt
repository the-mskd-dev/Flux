package com.mskd.flux.utils.extensions

import com.mskd.flux.model.player.PlayerTrack
import java.util.Locale


fun Locale.toPlayerTrack(type: PlayerTrack.Type) = PlayerTrack(
    id = null,
    label = this.displayName,
    language = this.language,
    type = type
)

fun Locale.toTmdbFormat() : String {
    return "${this.language}-${this.country}"
}