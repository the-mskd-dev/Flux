package com.mskd.flux.features.tmdb.data.model.mapper

import com.mskd.flux.features.tmdb.data.model.dto.SeasonDto
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