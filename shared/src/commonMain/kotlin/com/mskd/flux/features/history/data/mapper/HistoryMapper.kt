package com.mskd.flux.features.history.data.mapper

import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlin.time.Clock

fun HistoryEntity.toDomain() = HistoryEntry(
    id = this.id,
    artworkId = this.artworkId,
    type = this.type,
    title = this.title,
    description = this.description,
    duration = this.duration,
    currentTime = this.currentTime,
    timestamp = this.timestamp
)

fun HistoryEntry.toEntity() = HistoryEntity(
    id = this.id,
    artworkId = this.artworkId,
    type = this.type,
    title = this.title,
    description = this.description,
    duration = this.duration,
    currentTime = this.currentTime,
    timestamp = Clock.System.now().toEpochMilliseconds()
)