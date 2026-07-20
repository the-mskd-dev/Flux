package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.MovieEntity
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.files.UserFile

fun MovieEntity.toDomain() : Movie {
    val file = UserFile(
        name = this.fileName,
        addedDateTime = this.addedDateTime,
        path = this.path,
        source = this.source,
        parentDocId = this.parentDocId
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
        status = this.status,
        isAvailable = true
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
        source = this.file.source,
        parentDocId = this.file.parentDocId
    )
}