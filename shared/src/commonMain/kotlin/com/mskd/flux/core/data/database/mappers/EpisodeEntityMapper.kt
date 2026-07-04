package com.mskd.flux.core.data.database.mappers

import com.mskd.flux.core.data.database.model.EpisodeEntity
import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.files.UserFile

fun EpisodeEntity.toDomain() : Episode {
    val file = UserFile(
        name = this.fileName,
        addedDateTime = this.addedDateTime,
        path = this.path,
        source = this.source
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
        status = this.status
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
        source = this.file.source
    )
}