package com.mskd.flux.features.player.domain.mapper

import com.mskd.flux.core.model.language.languageCode
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.system.languageDisplayName

fun String.toPlayerTrack(type: PlayerTrack.Type) = PlayerTrack(
    id = null,
    label = languageDisplayName(this),
    language = languageCode(),
    type = type
)