package com.mskd.flux.features.tmdb.data.mapper

import com.mskd.flux.core.domain.model.artwork.Season
import com.mskd.flux.features.tmdb.data.dto.SeasonDto

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