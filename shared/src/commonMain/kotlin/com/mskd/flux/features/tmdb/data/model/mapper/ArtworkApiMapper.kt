package com.mskd.flux.features.tmdb.data.model.mapper

import com.mskd.flux.features.tmdb.data.model.dto.ArtworkDto
import com.mskd.flux.features.tmdb.data.model.dto.MediaTypeDto
import com.mskd.flux.model.domain.artwork.Artwork
import com.mskd.flux.model.domain.artwork.ContentType

fun ArtworkDto.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath.orEmpty(),
        bannerPath = this.bannerPath.orEmpty(),
        type = if (this.type == MediaTypeDto.MOVIE) ContentType.MOVIE else ContentType.SHOW
    )
}