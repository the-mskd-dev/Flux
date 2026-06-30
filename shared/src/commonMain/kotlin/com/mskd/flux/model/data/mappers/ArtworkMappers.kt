package com.mskd.flux.model.data.mappers

import com.mskd.flux.model.domain.artwork.Artwork
import com.mskd.flux.model.domain.artwork.ContentType
import com.mskd.flux.model.data.local.entities.ArtworkEntity
import com.mskd.flux.model.data.remote.tmdb.TMDBArtwork
import com.mskd.flux.model.data.remote.tmdb.TMDBMediaType
import com.mskd.flux.model.data.remote.tmdb.TMDBMovie

fun ArtworkEntity.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type
    )
}

fun Artwork.toEntity() : ArtworkEntity {
    return ArtworkEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type
    )
}

fun TMDBArtwork.toArtwork() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = if (this.type == TMDBMediaType.MOVIE) ContentType.MOVIE else ContentType.SHOW
    )
}

fun TMDBMovie.toArtwork() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = ContentType.MOVIE
    )
}