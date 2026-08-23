package com.mskd.flux.features.history.domain.model

import com.mskd.flux.core.model.artwork.ContentType

data class HistoryEntry(
    val id: Long,
    val artworkId: Long,
    val type: ContentType,
    val title: String,
    val description: String,
    val duration: Int,
    val currentTime: Long = 0L,
    val timestamp: Long,
)