package com.mskd.flux.model.data.mappers

import com.mskd.flux.model.domain.Status
import com.mskd.flux.model.domain.UserFile
import com.mskd.flux.model.domain.artwork.Movie
import com.mskd.flux.model.data.local.entities.MovieEntity
import com.mskd.flux.model.data.remote.tmdb.TMDBMovie

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

fun TMDBMovie.toDomain(
    file: UserFile,
    duration: Int
) : Movie {
    return Movie(
        artworkId = this.id,
        title = this.title,
        releaseDateString = this.releaseDate,
        description = this.description,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        duration = duration,
        currentTime = 0L,
        file = file,
        //genres = emptyList(),
        status = Status.TO_WATCH
    )
}