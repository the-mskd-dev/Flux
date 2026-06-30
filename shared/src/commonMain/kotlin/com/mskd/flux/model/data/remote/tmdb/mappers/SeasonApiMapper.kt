package com.mskd.flux.model.data.remote.tmdb.mappers

import com.mskd.flux.model.data.remote.tmdb.dto.SeasonDto
import com.mskd.flux.model.domain.artwork.Season

fun SeasonDto.toDomain(artworkId: Long) : Season {
    return Season(
        id = this.id,
        artworkId = artworkId,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        season = this.season
    )
}