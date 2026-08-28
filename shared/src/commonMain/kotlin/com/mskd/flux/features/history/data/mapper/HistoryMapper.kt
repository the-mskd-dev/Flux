package com.mskd.flux.features.history.data.mapper

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlin.time.Clock

fun HistoryEntity.toDomain() = HistoryEntry(
    id = this.id,
    artworkId = this.artworkId,
    type = this.type,
    title = this.title,
    season = this.season,
    number = this.number,
    path = this.path,
    duration = this.duration,
    currentTime = this.currentTime,
    timestamp = this.timestamp
)

fun HistoryEntry.toEntity() = HistoryEntity(
    id = this.id,
    artworkId = this.artworkId,
    type = this.type,
    title = this.title,
    season = this.season,
    number = this.number,
    path = this.path,
    duration = this.duration,
    currentTime = this.currentTime,
    timestamp = Clock.System.now().toEpochMilliseconds()
)

fun Media.toHistoryEntry()  = HistoryEntry(
    id = when (this) {
        is Episode -> this.id
        is Movie -> this.artworkId
    },
    artworkId = this.artworkId,
    type = when (this) {
        is Episode -> ContentType.SHOW
        is Movie -> ContentType.MOVIE
    },
    title = this.title,
    season = (this as? Episode)?.season,
    number = (this as? Episode)?.number,
    path = this.file.path,
    duration = this.duration,
    currentTime = this.currentTime,
    timestamp = Clock.System.now().toEpochMilliseconds()
)