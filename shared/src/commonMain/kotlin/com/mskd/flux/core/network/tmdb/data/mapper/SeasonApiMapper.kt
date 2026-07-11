package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.data.dto.SeasonDto

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