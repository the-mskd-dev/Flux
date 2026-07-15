package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.MovieDto

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
        status = Status.TO_WATCH,
        isAvailable = true
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