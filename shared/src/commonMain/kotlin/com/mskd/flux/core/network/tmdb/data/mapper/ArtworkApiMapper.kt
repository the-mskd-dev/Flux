package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.MediaTypeDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkCandidate
import kotlinx.collections.immutable.toImmutableList

fun ArtworkDto.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = if (this.type == MediaTypeDto.MOVIE) ContentType.MOVIE else ContentType.SHOW,
        genreIds = this.genreIds.toImmutableList(),
        nsfw = this.nsfw
    )
}

fun ArtworkDto.toCandidate() : ArtworkCandidate {
    return ArtworkCandidate(
        artwork = this.toDomain(),
        year = this.releaseDate?.take(4)?.toIntOrNull()
    )
}

fun MovieDto.toArtwork(logoPath: String? = null) : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = ContentType.MOVIE,
        genreIds = this.genres.map { it.id }.toImmutableList(),
        nsfw = this.nsfw,
        logoPath = logoPath
    )
}

fun ShowDto.toArtwork(logoPath: String? = null) : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = ContentType.SHOW,
        genreIds = this.genres.map { it.id }.toImmutableList(),
        nsfw = this.nsfw,
        logoPath = logoPath
    )
}
