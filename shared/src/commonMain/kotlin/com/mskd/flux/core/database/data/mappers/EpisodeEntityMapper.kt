package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.EpisodeEntity
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.files.UserFile

fun EpisodeEntity.toDomain() : Episode {
    val file = UserFile(
        name = this.fileName,
        addedDateTime = this.addedDateTime,
        path = this.path,
        source = this.source,
        parentDocId = this.parentDocId
    )

    return Episode(
        id = this.id,
        number = this.number,
        season = this.season,
        imagePath = this.imagePath,
        artworkId = this.artworkId,
        title = this.title,
        releaseDateString = this.releaseDateString,
        description = this.description,
        duration = this.duration,
        currentTime = this.currentTime,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        file = file,
        status = this.status,
        isAvailable = true
    )
}

fun Episode.toEntity() : EpisodeEntity {
    return EpisodeEntity(
        id = this.id,
        number = this.number,
        season = this.season,
        imagePath = this.imagePath,
        artworkId = this.artworkId,
        title = this.title,
        releaseDateString = this.releaseDateString,
        description = this.description,
        duration = this.duration,
        currentTime = this.currentTime,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        status = this.status,
        fileName = this.file.name,
        addedDateTime = this.file.addedDateTime,
        path = this.file.path,
        source = this.file.source,
        parentDocId = this.file.parentDocId
    )
}