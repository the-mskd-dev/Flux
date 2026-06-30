package com.mskd.flux.model.data.mappers

import com.mskd.flux.model.domain.Status
import com.mskd.flux.model.domain.UserFile
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.data.local.entities.EpisodeEntity
import com.mskd.flux.model.data.remote.tmdb.TMDBEpisode

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

fun TMDBEpisode.toDomain(
    artworkId: Long,
    file: UserFile,
    duration: Int
) : Episode {
    return Episode(
        id = this.id,
        artworkId = artworkId,
        title = this.title,
        number = this.number,
        season = this.season,
        imagePath = this.imagePath ?: "",
        releaseDateString = this.releaseDateString,
        description = this.description,
        duration = duration,
        currentTime = 0L,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        status = Status.TO_WATCH,
        file = file
    )
}