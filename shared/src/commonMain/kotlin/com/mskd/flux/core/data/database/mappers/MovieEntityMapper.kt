package com.mskd.flux.core.data.database.mappers

import com.mskd.flux.core.data.database.model.MovieEntity
import com.mskd.flux.core.domain.model.artwork.Movie
import com.mskd.flux.core.domain.model.files.UserFile

fun MovieEntity.toDomain() : Movie {
    val file = UserFile(
        name = this.fileName,
        addedDateTime = this.addedDateTime,
        path = this.path,
        source = this.source
    )

    return Movie(
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

fun Movie.toEntity() : MovieEntity {
    return MovieEntity(
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