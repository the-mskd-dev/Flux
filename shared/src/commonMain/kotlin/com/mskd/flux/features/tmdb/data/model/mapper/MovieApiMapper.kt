package com.mskd.flux.features.tmdb.data.model.mapper

import com.mskd.flux.features.tmdb.data.model.dto.MovieDto
import com.mskd.flux.model.domain.artwork.Artwork
import com.mskd.flux.model.domain.artwork.ContentType
import com.mskd.flux.model.domain.artwork.Movie
import com.mskd.flux.model.domain.artwork.Status
import com.mskd.flux.model.domain.files.UserFile

fun MovieDto.toDomain(
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

fun MovieDto.toDomainArtwork() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = ContentType.MOVIE
    )
}