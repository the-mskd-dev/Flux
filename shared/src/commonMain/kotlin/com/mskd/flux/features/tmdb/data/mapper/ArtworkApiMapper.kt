package com.mskd.flux.features.tmdb.data.mapper

import com.mskd.flux.features.tmdb.data.dto.ArtworkDto
import com.mskd.flux.features.tmdb.data.dto.MediaTypeDto
import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.ContentType

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