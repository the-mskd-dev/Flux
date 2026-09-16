package com.mskd.flux.features.history.domain.model

import com.mskd.flux.core.model.artwork.Media

data class HistoryEntry(
    val media: Media,
    val timestamp: Long,
    val title: String
)