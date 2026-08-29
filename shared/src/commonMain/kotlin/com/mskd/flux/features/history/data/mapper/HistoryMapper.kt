package com.mskd.flux.features.history.data.mapper

import com.mskd.flux.core.database.data.mappers.toDomain
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.data.model.HistoryProjection
import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlin.time.Clock

fun HistoryProjection.toDomain() = HistoryEntry(
    media = this.media.toDomain(),
    timestamp = this.history.timestamp
)

fun Media.toHistoryEntity() = HistoryEntity(
    mediaId = when (this) {
        is Episode -> this.id
        is Movie -> this.artworkId
    },
    artworkId = this.artworkId,
    timestamp = Clock.System.now().toEpochMilliseconds()
)

fun Media.toHistoryEntry() = HistoryEntry(
    media = this,
    timestamp = Clock.System.now().toEpochMilliseconds()
)

