package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.MediaTypeDto

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