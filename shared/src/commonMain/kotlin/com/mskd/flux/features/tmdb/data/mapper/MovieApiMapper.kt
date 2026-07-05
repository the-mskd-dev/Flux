package com.mskd.flux.features.tmdb.data.mapper

import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.artwork.Movie
import com.mskd.flux.core.domain.model.artwork.Status
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.tmdb.data.dto.MovieDto

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